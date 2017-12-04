package com.wonderkiln.camerakit;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
/*
Adapted/Copied from:
https://github.com/googlesamples/android-vision/blob/master/visionSamples/ocr-codelab/ocr-reader-complete/app/src/main/java/com/google/android/gms/samples/vision/ocrreader/ui/camera/CameraSource.java#L1080
*/

/**
 * This runnable controls access to the underlying receiver, calling it to process frames when
 * available from the camera.  This is designed to run detection on frames as fast as possible
 * (i.e., without unnecessary context switching or waiting on the next frame).
 * <p/>
 * While detection is running on a frame, new frames may be received from the camera.  As these
 * frames come in, the most recent frame is held onto as pending.  As soon as detection and its
 * associated processing are done for the previous frame, detection on the mostly recently
 * received frame will immediately start on the same thread.
 */
public class FrameProcessingRunnable implements Runnable {

    private static final String TAG = FrameProcessingRunnable.class.getSimpleName();
    private Detector<?> mDetector;
    private long mStartTimeMillis = android.os.SystemClock.elapsedRealtime();

    // This lock guards all of the member variables below.
    private final Object mLock = new Object();
    private boolean mActive = true;

    // These pending variables hold the state associated with the new frame awaiting processing.
    private long mPendingTimeMillis;
    private int mPendingFrameId = 0;
    private java.nio.ByteBuffer mPendingFrameData;
    private Thread mProcessingThread;

    /**
     * Map to convert between a byte array, received from the camera, and its associated byte
     * buffer.  We use byte buffers internally because this is a more efficient way to call into
     * native code later (avoids a potential copy).
     */
    private Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();
    private Size mPreviewSize;
    private Camera mCamera;

    public FrameProcessingRunnable(Detector<?> detector, Size mPreviewSize, Camera mCamera) {
        mDetector = detector;
        this.mPreviewSize = mPreviewSize;
        this.mCamera = mCamera;

        mProcessingThread = new Thread(this);
    }

    /**
     * Releases the underlying receiver.  This is only safe to do after the associated thread
     * has completed, which is managed in camera source's release method above.
     */
    @android.annotation.SuppressLint("Assert")
    public void release() {
        assert (mProcessingThread.getState() == Thread.State.TERMINATED);
        mDetector.release();
    }

    /**
     * As long as the processing thread is active, this executes detection on frames
     * continuously.  The next pending frame is either immediately available or hasn't been
     * received yet.  Once it is available, we transfer the frame info to local variables and
     * run detection on that frame.  It immediately loops back for the next frame without
     * pausing.
     * <p/>
     * If detection takes longer than the time in between new frames from the camera, this will
     * mean that this loop will run without ever waiting on a frame, avoiding any context
     * switching or frame acquisition time latency.
     * <p/>
     * If you find that this is using more CPU than you'd like, you should probably decrease the
     * FPS setting above to allow for some idle time in between frames.
     */
    @Override
    public void run() {
        Frame outputFrame;
        java.nio.ByteBuffer data;

        while (true) {
            synchronized (mLock) {
                while (mActive && (mPendingFrameData == null)) {
                    try {
                        // Wait for the next frame to be received from the camera, since we
                        // don't have it yet.
                        mLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                if (!mActive) {
                    // Exit the loop once this camera source is stopped or released.  We check
                    // this here, immediately after the wait() above, to handle the case where
                    // setActive(false) had been called, triggering the termination of this
                    // loop.
                    return;
                }

                if (mPreviewSize == null) {
                    // wait for this to be  set
                    Log.d("WHAT", "waitin for preview size to not be null");
                    continue;
                }

                outputFrame = new Frame.Builder()
                        .setImageData(mPendingFrameData, mPreviewSize.getWidth(),
                                mPreviewSize.getHeight(), android.graphics.ImageFormat.NV21)
                        .setId(mPendingFrameId)
                        .setTimestampMillis(mPendingTimeMillis)
                        .setRotation(0)
                        .build();

                // Hold onto the frame data locally, so that we can use this for detection
                // below.  We need to clear mPendingFrameData to ensure that this buffer isn't
                // recycled back to the camera before we are done using that data.
                data = mPendingFrameData;
                mPendingFrameData = null;
            }

            // The code below needs to run outside of synchronization, because this will allow
            // The code below needs to run outside of synchronization, because this will allow
            // the camera to add pending frame(s) while we are running detection on the current
            // frame.

            try {
                mDetector.receiveFrame(outputFrame);
            } catch (Throwable t) {
                Log.e(TAG, "Exception thrown from receiver.", t);
            } finally {
                mCamera.addCallbackBuffer(data.array());
            }
        }
    }

    public void cleanup() {
        // stop text dectection thread
        if (mProcessingThread != null) {
            try {
                // Wait for the thread to complete to ensure that we can't have multiple threads
                // executing at the same time (i.e., which would happen if we called start too
                // quickly after stop).
                mProcessingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            mProcessingThread = null;
        }

        setActive(false);
        mBytesToByteBuffer.clear();
    }

    public void start() {
        mProcessingThread = new Thread(this);
        setActive(true);
        mProcessingThread.start();

        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                setNextFrame(bytes, camera);
            }
        });
        mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        mCamera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
    }

    /**
     * Marks the runnable as active/not active.  Signals any blocked threads to continue.
     */
    private void setActive(boolean active) {
        synchronized (mLock) {
            mActive = active;
            mLock.notifyAll();
        }
    }

    /**
     * Sets the frame data received from the camera.  This adds the previous unused frame buffer
     * (if present) back to the camera, and keeps a pending reference to the frame data for
     * future use.
     */
    private void setNextFrame(byte[] data, Camera camera) {
        synchronized (mLock) {
            if (mPendingFrameData != null) {
                camera.addCallbackBuffer(mPendingFrameData.array());
                mPendingFrameData = null;
            }

            if (!mBytesToByteBuffer.containsKey(data)) {
                Log.d(TAG,
                        "Skipping frame.  Could not find ByteBuffer associated with the image " +
                                "data from the camera.");
                return;
            }

            // Timestamp and frame ID are maintained here, which will give downstream code some
            // idea of the timing of frames received and when frames were dropped along the way.
            mPendingTimeMillis = android.os.SystemClock.elapsedRealtime() - mStartTimeMillis;
            mPendingFrameId++;
            mPendingFrameData = mBytesToByteBuffer.get(data);

            // Notify the processor thread if it is waiting on the next frame (see below).
            mLock.notifyAll();
        }
    }

    private void addBuffer(byte[] byteArray, ByteBuffer buffer) {
        mBytesToByteBuffer.put(byteArray, buffer);
    }

    /**
     * Creates one buffer for the camera preview callback.  The size of the buffer is based off of
     * the camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        //
        // NOTICE: This code only works when using play services v. 8.1 or higher.
        //

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        addBuffer(byteArray, buffer);
        return byteArray;
    }
}
