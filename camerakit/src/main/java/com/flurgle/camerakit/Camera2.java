package com.flurgle.camerakit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;

import com.flurgle.camerakit.encoding.VideoEncoder;
import com.flurgle.camerakit.utils.AspectRatio;
import com.flurgle.camerakit.utils.Size;
import com.flurgle.camerakit.utils.YuvUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

@TargetApi(21)
public class Camera2 extends CameraViewImpl {

    private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();

    static {
        INTERNAL_FACINGS.put(CameraKit.Constants.FACING_BACK, CameraCharacteristics.LENS_FACING_BACK);
        INTERNAL_FACINGS.put(CameraKit.Constants.FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT);
    }

    private CameraManager mCameraManager;
    private CameraDevice mCamera;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private ImageReader mImageReader;
    private VideoEncoder mVideoEncoder;

    private Semaphore mCameraOpenCloseLock;

    private int mFacing;
    private int mFlash;
    private int mDisplayOrientation;
    private String mCameraId;
    private CameraCharacteristics mCameraCharacteristics;

    private SortedSet<Size> mPreviewSizes;
    private SortedSet<Size> mCaptureSizes;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private boolean mCropOutput;

    private boolean mIsRecording;

    Camera2(Context context, CameraListener cameraListener, PreviewImpl preview) {
        super(cameraListener, preview);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mPreview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {
                startCaptureSession();
            }
        });

        mCameraOpenCloseLock = new Semaphore(1);

        mPreviewSizes = new TreeSet<>();
        mCaptureSizes = new TreeSet<>();
    }

    @Override
    View getView() {
        return mPreview.getView();
    }

    @Override
    void start() {
        if (chooseCameraIdByFacing()) {
            startBackgroundThread();
            collectCameraInfo();
            prepareImageReader();
            startOpeningCamera();
        }
    }

    @Override
    void stop() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();

            if (mBackgroundThread != null) {
                stopBackgroundThread();
            }
        }

    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    void setFacing(int facing) {
        int internalFacing = INTERNAL_FACINGS.get(facing);
        if (mFacing == internalFacing) return;
        this.mFacing = internalFacing;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    int getFacing() {
        return mFacing;
    }

    @Override
    void setFlash(int flash) {
        if (mFlash == flash) return;
        int fallback = flash;
        mFlash = flash;
        if (mPreviewRequestBuilder != null) {
            updateFlash(mPreviewRequestBuilder);
            if (mCaptureSession != null) {
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    mFlash = fallback;
                }
            }
        }
    }

    @Override
    int getFlash() {
        return mFlash;
    }

    @Override
    boolean getAutoFocus() {
        return true;
    }

    @Override
    void capturePicture() {
        if (mFacing == INTERNAL_FACINGS.get(CameraKit.Constants.FACING_BACK)) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    @Override
    void captureStill() {
        if (mOnImageAvailableListener != null) {
            mOnImageAvailableListener.allowCallback();
        }
    }

    @Override
    void startVideo() {
        if (mCamera == null) {
            return;
        }

        mIsRecording = true;
    }

    @Override
    void endVideo() {
        mIsRecording = false;
        if (mVideoEncoder != null) {
            mVideoEncoder.stopEncoder();
            mVideoEncoder = null;
        }
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        mPreview.setDisplayOrientation(mDisplayOrientation);
    }

    void updateFlash(CaptureRequest.Builder builder) {
        switch (mFlash) {
            case CameraKit.Constants.FLASH_OFF:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                break;
            case CameraKit.Constants.FLASH_ON:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                break;
            case CameraKit.Constants.FLASH_AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                break;
        }
    }

    void setCropOutput(boolean cropOutput) {
        this.mCropOutput = cropOutput;
    }

    private boolean chooseCameraIdByFacing() {
        try {
            int internalFacing = mFacing;
            final String[] ids = mCameraManager.getCameraIdList();
            if (ids.length == 0) { // No camera
                throw new RuntimeException("No camera available.");
            }
            for (String id : ids) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                Integer level = characteristics.get(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if (level == null ||
                        level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    continue;
                }
                Integer internal = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (internal == null) {
                    throw new NullPointerException("Unexpected state: LENS_FACING null");
                }
                if (internal == internalFacing) {
                    mCameraId = id;
                    mCameraCharacteristics = characteristics;
                    return true;
                }
            }
            // Not found
            mCameraId = ids[0];
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            Integer level = mCameraCharacteristics.get(
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            if (level == null ||
                    level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                return false;
            }
            Integer internal = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (internal == null) {
                throw new NullPointerException("Unexpected state: LENS_FACING null");
            }
            for (int i = 0, count = INTERNAL_FACINGS.size(); i < count; i++) {
                if (INTERNAL_FACINGS.valueAt(i) == internal) {
                    mFacing = INTERNAL_FACINGS.keyAt(i);
                    return true;
                }
            }
            // The operation can reach here when the only camera device is an external one.
            // We treat it as facing back.
            mFacing = CameraKit.Constants.FACING_BACK;
            return true;
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get a list of camera devices", e);
        }
    }

    private void collectCameraInfo() {
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new IllegalStateException("Failed to get configuration map: " + mCameraId);
        }

        mPreviewSizes.clear();
        for (android.util.Size size : map.getOutputSizes(mPreview.getOutputClass())) {
            mPreviewSizes.add(new Size(size.getWidth(), size.getHeight()));
        }

        mCaptureSizes.clear();
        for (android.util.Size size : map.getOutputSizes(ImageFormat.JPEG)) {
            mCaptureSizes.add(new Size(size.getWidth(), size.getHeight()));
        }
    }

    private Size getOptimalPreviewSize() {
        int surfaceLonger, surfaceShorter;
        final int surfaceWidth = mPreview.getWidth();
        final int surfaceHeight = mPreview.getHeight();
        if (surfaceWidth < surfaceHeight) {
            surfaceLonger = surfaceHeight;
            surfaceShorter = surfaceWidth;
        } else {
            surfaceLonger = surfaceWidth;
            surfaceShorter = surfaceHeight;
        }
        // Pick the smallest of those big enough.
        for (Size size : mPreviewSizes) {
            if (size.getWidth() >= surfaceLonger && size.getHeight() >= surfaceShorter) {
                return size;
            }
        }
        // If no size is big enough, pick the largest one.
        return mPreviewSizes.last();
    }

    private void prepareImageReader() {
        Size previewSize = getOptimalPreviewSize();
        AspectRatio aspectRatio = AspectRatio.of(previewSize.getWidth(), previewSize.getHeight());
        Size bestSize = findSizeClosestTo(1500000, aspectRatio, mCaptureSizes);
        mImageReader = ImageReader.newInstance(bestSize.getWidth(), bestSize.getHeight(), ImageFormat.YUV_420_888, 3);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
    }

    private Size findSizeClosestTo(int targetLength, AspectRatio targetAspectRatio, SortedSet<Size> sizes) {
        int closestDistance = Integer.MAX_VALUE;
        Size closestSize = null;
        for (Size size : sizes) {
            if (targetAspectRatio.matches(size)) {
                int length = size.getWidth() * size.getHeight();
                int distance = Math.abs(targetLength - length);
                if (closestSize == null) {
                    closestSize = size;
                } else {
                    if (distance < closestDistance) {
                        closestSize = size;
                        closestDistance = length;
                    }
                }
            }
        }

        return closestSize;
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("MissingPermission")
    private void startOpeningCamera() {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraManager.openCamera(mCameraId, mCameraDeviceCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to open camera: " + mCameraId, e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Time out waiting to lock camera opening.");
        }
    }

    void startCaptureSession() {
        if (!isCameraOpened() || !mPreview.isReady() || mImageReader == null) {
            return;
        }

        Size previewSize = getOptimalPreviewSize();

        mPreview.setTruePreviewSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = mPreview.getSurface();
        try {
            mPreviewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            mCamera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to start camera session");
        } catch (IllegalStateException e) {
            startOpeningCamera();
        }
    }

    private void lockFocus() {
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to lock focus.", e);
        }
    }

    private void captureStillPicture() {
        try {
            CaptureRequest.Builder captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            updateFlash(captureRequestBuilder);
            // Calculate JPEG orientation.
            @SuppressWarnings("ConstantConditions")
            int sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    (sensorOrientation +
                            mDisplayOrientation * (mFacing == CameraKit.Constants.FACING_FRONT ? 1 : -1) +
                            360) % 360);
            // Stop preview and capture a still picture.
            mCaptureSession.stopRepeating();

            mOnImageAvailableListener.allowCallback();
            mCaptureSession.capture(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot capture a still picture.", e);
        }
    }

    void unlockFocus() {
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        try {
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, null);
            //updateAutoFocus();
            updateFlash(mPreviewRequestBuilder);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mCaptureCallback.setState(PictureCaptureCallback.STATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to restart camera preview.", e);
        }
    }

    private final CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCamera = camera;
            getCameraListener().onCameraOpened();
            startCaptureSession();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            getCameraListener().onCameraClosed();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCamera.close();
            mCamera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            Log.e(TAG, "onError: " + camera.getId() + " (" + error + ")");
            mCamera = null;
        }

    };

    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (mCamera == null) {
                return;
            }
            mCaptureSession = session;
            updateFlash(mPreviewRequestBuilder);
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to start camera preview.", e);
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Failed to configure capture session.");
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            if (mCaptureSession != null && mCaptureSession.equals(session)) {
                mCaptureSession = null;
            }
        }

    };

    private PictureCaptureCallback mCaptureCallback = new PictureCaptureCallback() {

        @Override
        public void onPrecaptureRequired() {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(STATE_PRECAPTURE);
            try {
                mCaptureSession.capture(mPreviewRequestBuilder.build(), this, mBackgroundHandler);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to run precapture sequence.", e);
            }
        }

        @Override
        public void onReady() {
            captureStillPicture();
        }

    };


    private abstract static class VariableCallbackOnImageAvailableListener implements ImageReader.OnImageAvailableListener {

        protected boolean mAllowOneCallback = false;

        @Override
        public abstract void onImageAvailable(ImageReader reader);

        public void allowCallback() {
            mAllowOneCallback = true;
        }

    }

    private VariableCallbackOnImageAvailableListener mOnImageAvailableListener = new VariableCallbackOnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (!mAllowOneCallback && !mIsRecording) {
                image.close();
                return;
            }

            Rect crop = null;
            if (mCropOutput) {

            }

            if (mAllowOneCallback) {
                mAllowOneCallback = false;
                byte[] out = YuvUtils.createRGB(image, crop);
                getCameraListener().onPictureTaken(out);
            }

            if (mIsRecording) {
                if (mVideoEncoder == null) {
                    try {
                        mVideoEncoder = new VideoEncoder(getView().getContext(), mFacing, image.getWidth(), image.getHeight());
                    } catch (IOException e) {

                    }
                }

                if (mVideoEncoder != null) {
                    byte[] out = YuvUtils.getYUVData(image);
                    try {
                        mVideoEncoder.encode(out);
                    } catch (Exception e) {

                    }
                }
            }

            image.close();
        }
    };

}
