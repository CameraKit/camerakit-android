package com.wonderkiln.camerakit;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.wonderkiln.camerakit.CameraKit.Constants.FLASH_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_CONTINUOUS;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_TAP;
import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STANDARD;
import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STILL;

@SuppressWarnings("deprecation")
public class Camera1 extends CameraImpl {

    private static final String TAG = Camera1.class.getSimpleName();

    private static final int FOCUS_AREA_SIZE_DEFAULT = 300;
    private static final int FOCUS_METERING_AREA_WEIGHT_DEFAULT = 1000;
    private static final int DELAY_MILLIS_BEFORE_RESETTING_FOCUS = 3000;

    private int mCameraId;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private CameraProperties mCameraProperties;
    private Camera.CameraInfo mCameraInfo;
    private Size mCaptureSize;
    private Size mVideoSize;
    private Size mPreviewSize;
    private MediaRecorder mMediaRecorder;
    private Camera.AutoFocusCallback mAutofocusCallback;
    private boolean capturingImage = false;

    private boolean mShowingPreview;
    private boolean mRecording;
    private int mDisplayOrientation;
    private int mDeviceOrientation;

    @Facing
    private int mFacing;

    @Flash
    private int mFlash;

    @Focus
    private int mFocus;

    @CaptureMethod
    private int mMethod;

    @Zoom
    private int mZoom;

    @VideoQuality
    private int mVideoQuality;

    private int mVideoBitRate;

    private boolean mLockVideoAspectRatio;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Handler mHandler = new Handler();

    private VideoCapturedCallback mVideoCallback;

    private final Object mCameraLock = new Object();

    Camera1(EventDispatcher eventDispatcher, PreviewImpl preview) {
        super(eventDispatcher, preview);
        mCameraInfo = new Camera.CameraInfo();
    }

    // CameraImpl:

    @Override
    void start() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return;
            }

            setFacing(mFacing);

            mCamera = Camera.open(mCameraId);
            mCameraParameters = mCamera.getParameters();

            collectCameraProperties();
            adjustCameraParameters();

            mEventDispatcher.dispatch(new CameraKitEvent(CameraKitEvent.TYPE_CAMERA_OPEN));

            mPreview.setCallback(new PreviewImpl.Callback() {
                @Override
                public void onSurfaceChanged(SurfaceHolder surfaceHolder) {
                    synchronized (mCameraLock) {
                        if (mCamera != null) {
                            if (mShowingPreview) {
                                mCamera.stopPreview();
                                mShowingPreview = false;
                            }

                            setDisplayAndDeviceOrientation();
                            setupPreview(surfaceHolder);

                            if (!mShowingPreview) {
                                mCamera.startPreview();
                                mShowingPreview = true;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    void stop() {
        mHandler.removeCallbacksAndMessages(null);
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                notifyErrorListener(e);
            }
        }
        mPreview.setCallback(null);

        mShowingPreview = false;

        releaseMediaRecorder();
        releaseCamera();
    }

    void setDisplayAndDeviceOrientation() {
        setDisplayAndDeviceOrientation(this.mDisplayOrientation, this.mDeviceOrientation);
    }

    @Override
    void setDisplayAndDeviceOrientation(int displayOrientation, int deviceOrientation) {
        this.mDisplayOrientation = displayOrientation;
        this.mDeviceOrientation = deviceOrientation;

        synchronized (mCameraLock) {
            if (isCameraOpened()) {
                try {
                    mCamera.setDisplayOrientation(calculatePreviewRotation());
                } catch (RuntimeException e) {
                    // Camera is released. Ignore. Orientations are still valid in local member fields
                    // so next time camera starts it will have correct configuration.
                }
            }
        }
    }

    @Override
    void setFacing(@Facing int facing) {
        synchronized (mCameraLock) {
            int internalFacing = new ConstantMapper.Facing(facing).map();
            if (internalFacing == -1) {
                return;
            }

            for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
                Camera.getCameraInfo(i, mCameraInfo);
                if (mCameraInfo.facing == internalFacing) {
                    mCameraId = i;
                    mFacing = facing;
                    break;
                }
            }

            if (mFacing == facing && isCameraOpened()) {
                stop();
                start();
            }
        }
    }

    @Override
    void setFlash(@Flash int flash) {
        synchronized (mCameraLock) {
            if (mCameraParameters != null) {
                List<String> flashes = mCameraParameters.getSupportedFlashModes();
                String internalFlash = new ConstantMapper.Flash(flash).map();
                if (flashes != null && flashes.contains(internalFlash)) {
                    mCameraParameters.setFlashMode(internalFlash);
                    mFlash = flash;
                } else {
                    String currentFlash = new ConstantMapper.Flash(mFlash).map();
                    if (flashes == null || !flashes.contains(currentFlash)) {
                        mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mFlash = FLASH_OFF;
                    }
                }

                mCamera.setParameters(mCameraParameters);
            } else {
                mFlash = flash;
            }
        }
    }

    @Override
    void setFocus(@Focus int focus) {
        synchronized (mCameraLock) {
            this.mFocus = focus;
            switch (focus) {
                case FOCUS_CONTINUOUS:
                    if (mCameraParameters != null) {
                        detachFocusTapListener();
                        final List<String> modes = mCameraParameters.getSupportedFocusModes();
                        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        } else {
                            setFocus(FOCUS_OFF);
                        }
                    }
                    break;

                case FOCUS_TAP:
                    if (mCameraParameters != null) {
                        attachFocusTapListener();
                        final List<String> modes = mCameraParameters.getSupportedFocusModes();
                        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }
                    }
                    break;

                case FOCUS_OFF:
                    if (mCameraParameters != null) {
                        detachFocusTapListener();
                        final List<String> modes = mCameraParameters.getSupportedFocusModes();
                        if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                        } else {
                            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    void setMethod(@CaptureMethod int method) {
        this.mMethod = method;
    }

    @Override
    void setZoom(@Zoom int zoom) {
        this.mZoom = zoom;
    }

    @Override
    void setVideoQuality(int videoQuality) {
        this.mVideoQuality = videoQuality;
    }

    @Override
    void setVideoBitRate(int videoBitRate) {
        this.mVideoBitRate = videoBitRate;
    }

    @Override
    void setLockVideoAspectRatio(boolean lockVideoAspectRatio) {
        this.mLockVideoAspectRatio = lockVideoAspectRatio;
    }

    @Override
    void captureImage(final ImageCapturedCallback callback) {
        switch (mMethod) {
            case METHOD_STANDARD:
                synchronized (mCameraLock) {
                    // Null check required for camera here as is briefly null when View is detached
                    if (!capturingImage && mCamera != null) {

                        // Set boolean to wait for image callback
                        capturingImage = true;

                        // Set the captureRotation right before taking a picture so it's accurate
                        int captureRotation = calculateCaptureRotation();
//                        mCameraParameters.setRotation(captureRotation);
                        mCamera.setParameters(mCameraParameters);

                        mCamera.takePicture(null, null, null,
                                new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        callback.imageCaptured(data, calculateCaptureRotation());

                                        // Reset capturing state to allow photos to be taken
                                        capturingImage = false;

                                        synchronized (mCameraLock) {
                                            if (isCameraOpened()) {
                                                try {
                                                    stop();
                                                    start();
                                                } catch (Exception e) {
                                                    notifyErrorListener(e);
                                                }
                                            }
                                        }
                                    }
                                });
                    } else {
                        Log.w(TAG, "Unable, waiting for picture to be taken");
                    }
                    break;
                }

            case METHOD_STILL:
                synchronized (mCameraLock) {
                    mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            Camera.Parameters parameters = camera.getParameters();
                            int width = parameters.getPreviewSize().width;
                            int height = parameters.getPreviewSize().height;
                            int rotation = calculateCaptureRotation();

                            YuvOperator yuvOperator = new YuvOperator(data, width, height);
                            yuvOperator.rotate(rotation);
                            data = yuvOperator.getYuvData();

                            int yuvOutputWidth = width;
                            int yuvOutputHeight = height;
                            if (rotation == 90 || rotation == 270) {
                                yuvOutputWidth = height;
                                yuvOutputHeight = width;
                            }

                            YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), yuvOutputWidth, yuvOutputHeight, null);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
                            callback.imageCaptured(out.toByteArray(), 0);
                        }
                    });
                    break;
                }
        }
    }

    @Override
    void captureVideo(VideoCapturedCallback callback) {
        synchronized (mCameraLock) {
            try {
                if (prepareMediaRecorder()) {
                    mMediaRecorder.start();
                    mRecording = true;
                    this.mVideoCallback = callback;
                } else {
                    releaseMediaRecorder();
                }
            } catch (IOException e) {
                releaseMediaRecorder();
            } catch (RuntimeException e) {
                releaseMediaRecorder();
            }
        }
    }

    @Override
    void stopVideo() {
        synchronized (mCameraLock) {
            if (mRecording) {
                File videoFile = getVideoFile();

                try {
                    mMediaRecorder.stop();
                    if (this.mVideoCallback != null) {
                        mVideoCallback.videoCaptured(videoFile);
                        mVideoCallback = null;
                    }
                } catch (RuntimeException e) {
                    videoFile.delete();
                }

                releaseMediaRecorder();
                mRecording = false;
            }

            stop();
            start();
        }
    }

    @Override
    Size getCaptureResolution() {
        if (mCaptureSize == null && mCameraParameters != null) {
            TreeSet<Size> sizes = new TreeSet<>();
            for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
                sizes.add(new Size(size.width, size.height));
            }

            TreeSet<AspectRatio> aspectRatios = findCommonAspectRatios(
                    mCameraParameters.getSupportedPreviewSizes(),
                    mCameraParameters.getSupportedPictureSizes()
            );
            AspectRatio targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;

            Iterator<Size> descendingSizes = sizes.descendingIterator();
            Size size;
            while (descendingSizes.hasNext() && mCaptureSize == null) {
                size = descendingSizes.next();
                if (targetRatio == null || targetRatio.matches(size)) {
                    mCaptureSize = size;
                    break;
                }
            }
        }

        return mCaptureSize;
    }

    @Override
    Size getVideoResolution() {
        if (mVideoSize == null && mCameraParameters != null) {
            if (mCameraParameters.getSupportedVideoSizes() == null) {
                mVideoSize = getCaptureResolution();
                return mVideoSize;
            }

            TreeSet<Size> sizes = new TreeSet<>();
            for (Camera.Size size : mCameraParameters.getSupportedVideoSizes()) {
                sizes.add(new Size(size.width, size.height));
            }

            TreeSet<AspectRatio> aspectRatios = findCommonAspectRatios(
                    mCameraParameters.getSupportedPreviewSizes(),
                    mCameraParameters.getSupportedVideoSizes()
            );
            AspectRatio targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;

            Iterator<Size> descendingSizes = sizes.descendingIterator();
            Size size;
            while (descendingSizes.hasNext() && mVideoSize == null) {
                size = descendingSizes.next();
                if (targetRatio == null || targetRatio.matches(size)) {
                    mVideoSize = size;
                    break;
                }
            }
        }

        return mVideoSize;
    }

    @Override
    Size getPreviewResolution() {
        if (mPreviewSize == null && mCameraParameters != null) {
            TreeSet<Size> sizes = new TreeSet<>();
            for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
                sizes.add(new Size(size.width, size.height));
            }

            TreeSet<AspectRatio> aspectRatios = findCommonAspectRatios(
                    mCameraParameters.getSupportedPreviewSizes(),
                    mCameraParameters.getSupportedPictureSizes()
            );

            AspectRatio targetRatio = null;

            if (mLockVideoAspectRatio) {
                TreeSet<AspectRatio> videoAspectRatios = findCommonAspectRatios(
                        mCameraParameters.getSupportedPreviewSizes(),
                        mCameraParameters.getSupportedPictureSizes()
                );

                Iterator<AspectRatio> descendingIterator = aspectRatios.descendingIterator();
                while (targetRatio == null && descendingIterator.hasNext()) {
                    AspectRatio ratio = descendingIterator.next();
                    if (videoAspectRatios.contains(ratio)) {
                        targetRatio = ratio;
                    }
                }
            }

            if (targetRatio == null) {
                targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;
            }

            Iterator<Size> descendingSizes = sizes.descendingIterator();
            Size size;
            while (descendingSizes.hasNext() && mPreviewSize == null) {
                size = descendingSizes.next();
                if (targetRatio == null || targetRatio.matches(size)) {
                    mPreviewSize = size;
                    break;
                }
            }
        }

        boolean invertPreviewSizes = (mCameraInfo.orientation + mDeviceOrientation) % 180 == 90;
        if (mPreviewSize != null && invertPreviewSizes) {
            return new Size(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }

        return mPreviewSize;
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    boolean frontCameraOnly() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        boolean isFrontCameraOnly = (Camera.getNumberOfCameras() == 1 && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        return isFrontCameraOnly;
    }

    @Nullable
    @Override
    CameraProperties getCameraProperties() {
        return mCameraProperties;
    }

    // Internal:

    private void setupPreview(SurfaceHolder surfaceHolder) {
        synchronized (mCameraLock) {
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void releaseCamera() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
                mCameraParameters = null;
                mPreviewSize = null;
                mCaptureSize = null;
                mVideoSize = null;

                mEventDispatcher.dispatch(new CameraKitEvent(CameraKitEvent.TYPE_CAMERA_CLOSE));
            }
        }
    }

    private int calculatePreviewRotation() {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - ((mCameraInfo.orientation + mDisplayOrientation) % 360)) % 360;
        } else {
            return (mCameraInfo.orientation - mDisplayOrientation + 360) % 360;
        }
    }

    private int calculateCaptureRotation() {
        int captureRotation = 0;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            captureRotation = (mCameraInfo.orientation + mDisplayOrientation) % 360;
        } else {  // back-facing camera
            captureRotation = (mCameraInfo.orientation - mDisplayOrientation + 360) % 360;
        }

//        // Accommodate for any extra device rotation relative to fixed screen orientations
//        // (e.g. activity fixed in portrait, but user took photo/video in landscape)
//        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            captureRotation = ((captureRotation - (mDisplayOrientation - mDeviceOrientation)) + 360) % 360;
//        } else {  // back-facing camera
//            captureRotation = (captureRotation + (mDisplayOrientation - mDeviceOrientation) + 360) % 360;
//        }

        return captureRotation;
    }

    private void notifyErrorListener(@NonNull final String details) {
        CameraKitError error = new CameraKitError();
        error.setMessage(details);
        mEventDispatcher.dispatch(error);
    }

    private void notifyErrorListener(@NonNull final Exception e) {
        CameraKitError error = new CameraKitError(e);
        mEventDispatcher.dispatch(error);
    }

    private Camera.Parameters getCameraParameters() {
        if (mCamera != null) {
            try {
                return mCamera.getParameters();
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private void adjustCameraParameters() {
        synchronized (mCameraLock) {
            if (mShowingPreview) {
                mCamera.stopPreview();
            }

            adjustCameraParameters(0);

            if (mShowingPreview) {
                mCamera.startPreview();
            }
        }
    }

    private void adjustCameraParameters(int currentTry) {
        boolean haveToReadjust = false;
        Camera.Parameters resolutionLess = mCamera.getParameters();

        if (getPreviewResolution() != null) {
            mPreview.setPreviewParameters(
                    getPreviewResolution().getWidth(),
                    getPreviewResolution().getHeight(),
                    mCameraParameters.getPreviewFormat()
            );

            mCameraParameters.setPreviewSize(
                    getPreviewResolution().getWidth(),
                    getPreviewResolution().getHeight()
            );

            try {
                mCamera.setParameters(mCameraParameters);
                resolutionLess = mCameraParameters;
            } catch (Exception e) {
                notifyErrorListener(e);
                // Some phones can't set parameters that camerakit has chosen, so fallback to defaults
                mCameraParameters = resolutionLess;
            }
        } else {
            haveToReadjust = true;
        }

        if (getCaptureResolution() != null) {
            mCameraParameters.setPictureSize(
                    getCaptureResolution().getWidth(),
                    getCaptureResolution().getHeight()
            );

            try {
                mCamera.setParameters(mCameraParameters);
                resolutionLess = mCameraParameters;
            } catch (Exception e) {
                notifyErrorListener(e);
                //Some phones can't set parameters that camerakit has chosen, so fallback to defaults
                mCameraParameters = resolutionLess;
            }
        } else {
            haveToReadjust = true;
        }

        int rotation = calculateCaptureRotation();
//        mCameraParameters.setRotation(rotation);

        setFocus(mFocus);

        try {
            setFlash(mFlash);
        } catch (Exception e) {
            notifyErrorListener(e);
        }

        mCamera.setParameters(mCameraParameters);

        if (haveToReadjust && currentTry < 100) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            notifyErrorListener(String.format("retryAdjustParam Failed, attempt #: %d", currentTry));
            adjustCameraParameters(currentTry + 1);
        }
    }

    private void collectCameraProperties() {
        mCameraProperties = new CameraProperties(mCameraParameters.getVerticalViewAngle(),
                mCameraParameters.getHorizontalViewAngle());
    }

    private TreeSet<AspectRatio> findCommonAspectRatios(List<Camera.Size> previewSizes, List<Camera.Size> pictureSizes) {
        Set<AspectRatio> previewAspectRatios = new HashSet<>();
        for (Camera.Size size : previewSizes) {
            AspectRatio deviceRatio = AspectRatio.of(CameraKit.Internal.screenHeight, CameraKit.Internal.screenWidth);
            AspectRatio previewRatio = AspectRatio.of(size.width, size.height);
            if (deviceRatio.equals(previewRatio)) {
                previewAspectRatios.add(previewRatio);
            }
        }

        Set<AspectRatio> captureAspectRatios = new HashSet<>();
        for (Camera.Size size : pictureSizes) {
            captureAspectRatios.add(AspectRatio.of(size.width, size.height));
        }

        TreeSet<AspectRatio> output = new TreeSet<>();
        if (previewAspectRatios.size() == 0) {
            // if no common aspect ratios
            Camera.Size maxSize = previewSizes.get(0);
            AspectRatio maxPreviewAspectRatio = AspectRatio.of(maxSize.width, maxSize.height);
            for (AspectRatio aspectRatio : captureAspectRatios) {
                if (aspectRatio.equals(maxPreviewAspectRatio)) {
                    output.add(aspectRatio);
                }
            }
        } else {
            // if common aspect ratios exist
            for (AspectRatio aspectRatio : previewAspectRatios) {
                if (captureAspectRatios.contains(aspectRatio)) {
                    output.add(aspectRatio);
                }
            }
        }

        return output;
    }

    private boolean prepareMediaRecorder() throws IOException {
        synchronized (mCameraLock) {
            mCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mCamera);

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            CamcorderProfile profile = getCamcorderProfile(mVideoQuality);
            mMediaRecorder.setProfile(profile);

            File videoFile = getVideoFile();
            if (videoFile == null) {
                return false;
            }

            mMediaRecorder.setOutputFile(videoFile.getPath());
            mMediaRecorder.setPreviewDisplay(mPreview.getSurface());
            mMediaRecorder.setOrientationHint(calculateCaptureRotation());

            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                releaseMediaRecorder();
                return false;
            }

            return true;
        }
    }

    private void releaseMediaRecorder() {
        synchronized (mCameraLock) {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
            }
        }
    }

    private File getVideoFile() {
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        return new File(mediaStorageDir.getPath() + File.separator + "video.mp4");
    }

    private CamcorderProfile getCamcorderProfile(@VideoQuality int videoQuality) {
        CamcorderProfile camcorderProfile = null;
        switch (videoQuality) {
            case CameraKit.Constants.VIDEO_QUALITY_QVGA:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_QVGA)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_QVGA);
                } else {
                    camcorderProfile = getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_LOWEST);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_480P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    camcorderProfile = getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_QVGA);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_720P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_720P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_720P);
                } else {
                    camcorderProfile = getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_480P);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_1080P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_1080P);
                } else {
                    camcorderProfile = getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_720P);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_2160P:
                try {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_2160P);
                } catch (Exception e) {
                    camcorderProfile = getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_HIGHEST);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_HIGHEST:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);
                break;

            case CameraKit.Constants.VIDEO_QUALITY_LOWEST:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
                break;
        }

        if (camcorderProfile != null && mVideoBitRate != 0) {
            camcorderProfile.videoBitRate = mVideoBitRate;
        }

        return camcorderProfile;
    }

    void setTapToAutofocusListener(Camera.AutoFocusCallback callback) {
        if (this.mFocus != FOCUS_TAP) {
            throw new IllegalArgumentException("Please set the camera to FOCUS_TAP.");
        }

        this.mAutofocusCallback = callback;
    }

    private int getFocusAreaSize() {
        return FOCUS_AREA_SIZE_DEFAULT;
    }

    private int getFocusMeteringAreaWeight() {
        return FOCUS_METERING_AREA_WEIGHT_DEFAULT;
    }

    private void detachFocusTapListener() {
        mPreview.getView().setOnTouchListener(null);
    }

    private void attachFocusTapListener() {
        mPreview.getView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    synchronized (mCameraLock) {
                        if (mCamera != null) {
                            Camera.Parameters parameters = getCameraParameters();
                            if (parameters == null) return false;

                            String focusMode = parameters.getFocusMode();
                            Rect rect = calculateFocusArea(event.getX(), event.getY());
                            List<Camera.Area> meteringAreas = new ArrayList<>();
                            meteringAreas.add(new Camera.Area(rect, getFocusMeteringAreaWeight()));
                            if (parameters.getMaxNumFocusAreas() != 0 && focusMode != null &&
                                    (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                                            focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ||
                                            focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ||
                                            focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                                    ) {
                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                parameters.setFocusAreas(meteringAreas);
                                if (parameters.getMaxNumMeteringAreas() > 0) {
                                    parameters.setMeteringAreas(meteringAreas);
                                }
                                if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                    return false; //cannot autoFocus
                                }
                                mCamera.setParameters(parameters);
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        resetFocus(success, camera);
                                    }
                                });
                            } else if (parameters.getMaxNumMeteringAreas() > 0) {
                                if (!parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                    return false; //cannot autoFocus
                                }
                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                parameters.setFocusAreas(meteringAreas);
                                parameters.setMeteringAreas(meteringAreas);

                                mCamera.setParameters(parameters);
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        resetFocus(success, camera);
                                    }
                                });
                            } else {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        if (mAutofocusCallback != null) {
                                            mAutofocusCallback.onAutoFocus(success, camera);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    private void resetFocus(final boolean success, final Camera camera) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                synchronized (mCameraLock) {
                    if (mCamera != null) {
                        mCamera.cancelAutoFocus();
                        Camera.Parameters parameters = getCameraParameters();
                        if (parameters == null) return;

                        if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            parameters.setFocusAreas(null);
                            parameters.setMeteringAreas(null);
                            mCamera.setParameters(parameters);
                        }

                        if (mAutofocusCallback != null) {
                            mAutofocusCallback.onAutoFocus(success, mCamera);
                        }
                    }
                }
            }
        }, DELAY_MILLIS_BEFORE_RESETTING_FOCUS);
    }

    private Rect calculateFocusArea(float x, float y) {
        int buffer = getFocusAreaSize() / 2;
        int centerX = calculateCenter(x, mPreview.getView().getWidth(), buffer);
        int centerY = calculateCenter(y, mPreview.getView().getHeight(), buffer);
        return new Rect(
                centerX - buffer,
                centerY - buffer,
                centerX + buffer,
                centerY + buffer
        );
    }

    private static int calculateCenter(float coord, int dimen, int buffer) {
        int normalized = (int) ((coord / dimen) * 2000 - 1000);
        if (Math.abs(normalized) + buffer > 1000) {
            if (normalized > 0) {
                return 1000 - buffer;
            } else {
                return -1000 + buffer;
            }
        } else {
            return normalized;
        }
    }

}
