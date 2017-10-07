package com.wonderkiln.camerakit;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

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
    private File mVideoFile;
    private Camera.AutoFocusCallback mAutofocusCallback;
    private boolean capturingImage = false;

    private boolean mShowingPreview;
    private int mDisplayOrientation;
    private int mDeviceOrientation;

    @Facing
    private int mFacing;

    @Flash
    private int mFlash;

    @Focus
    private int mFocus;

    @Method
    private int mMethod;

    @Zoom
    private int mZoom;

    @VideoQuality
    private int mVideoQuality;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Handler mHandler = new Handler();

    private final Object mCameraLock = new Object();

    @Nullable
    private ErrorListener mErrorListener;

    Camera1(CameraListener callback, PreviewImpl preview) {
        super(callback, preview);
        preview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {
                if (mCamera != null) {
                    if (mShowingPreview) {
                        mCamera.stopPreview();
                        mShowingPreview = false;
                    }
                    setDisplayAndDeviceOrientation();
                    setupPreview();
                    if (!mShowingPreview) {
                        mCamera.startPreview();
                        mShowingPreview = true;
                    }
                }
            }
        });

        mCameraInfo = new Camera.CameraInfo();
    }

    // CameraImpl:

    @Override
    void start() {
        setFacing(mFacing);
        openCamera();
        if (mPreview.isReady()) {
            setDisplayAndDeviceOrientation();
            setupPreview();
            mCamera.startPreview();
            mShowingPreview = true;
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
        mShowingPreview = false;
        releaseCamera();
    }

    void setDisplayAndDeviceOrientation() {
        setDisplayAndDeviceOrientation(this.mDisplayOrientation, this.mDeviceOrientation);
    }

    @Override
    void setDisplayAndDeviceOrientation(int displayOrientation, int deviceOrientation) {
        this.mDisplayOrientation = displayOrientation;
        this.mDeviceOrientation = deviceOrientation;

        if (isCameraOpened()) {
            mCamera.setDisplayOrientation(calculatePreviewRotation());
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
    void setMethod(@Method int method) {
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
    void captureImage() {
        switch (mMethod) {
            case METHOD_STANDARD:
                synchronized (mCameraLock) {
                    // Null check required for camera here as is briefly null when View is detached
                    if (!capturingImage && mCamera != null) {

                        // Set boolean to wait for image callback
                        capturingImage = true;

                        // Set the captureRotation right before taking a picture so it's accurate
                        int captureRotation = calculateCaptureRotation();
                        mCameraParameters.setRotation(captureRotation);
                        mCamera.setParameters(mCameraParameters);

                        mCamera.takePicture(null, null, null,
                                new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        mCameraListener.onPictureTaken(data);

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
                            new Thread(new ProcessStillTask(data, camera, calculateCaptureRotation(), new ProcessStillTask.OnStillProcessedListener() {
                                @Override
                                public void onStillProcessed(final YuvImage yuv) {
                                    mCameraListener.onPictureTaken(yuv);
                                }
                            })).start();
                        }
                    });
                    break;
                }
        }
    }

    @Override
    void startVideo() {
        synchronized (mCameraLock) {
            initMediaRecorder();
            prepareMediaRecorder();
            mMediaRecorder.start();
        }
    }

    @Override
    void endVideo() {
        synchronized (mCameraLock) {
            try {
                mMediaRecorder.stop();
                mCameraListener.onVideoTaken(mVideoFile);
            } catch (RuntimeException e) {
                mVideoFile.delete();
                mCameraListener.onVideoTaken(null);
            } finally {
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
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
            AspectRatio targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;

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

    void setErrorListener(ErrorListener listener) {
        mErrorListener = listener;
    }

    @Nullable
    @Override
    CameraProperties getCameraProperties() {
        return mCameraProperties;
    }

    // Internal:

    private void openCamera() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                releaseCamera();
            }

            mCamera = Camera.open(mCameraId);
            mCameraParameters = mCamera.getParameters();

            collectCameraProperties();
            adjustCameraParameters();

            mCameraListener.onCameraOpened();
        }
    }

    private void setupPreview() {
        synchronized (mCameraLock) {
            try {
                if (mPreview.getOutputClass() == SurfaceHolder.class) {
                    mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                } else {
                    mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void releaseCamera() {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                mCamera.lock();
                mCamera.release();
                mCamera = null;
                mCameraParameters = null;
                mPreviewSize = null;
                mCaptureSize = null;
                mVideoSize = null;
                mCameraListener.onCameraClosed();
            }
        }
    }

    private int calculatePreviewRotation() {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            int result = (mCameraInfo.orientation + mDisplayOrientation) % 360;
            return (360 - result) % 360;
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

        // Accommodate for any extra device rotation relative to fixed screen orientations
        // (e.g. activity fixed in portrait, but user took photo/video in landscape)
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            captureRotation = ((captureRotation - (mDisplayOrientation - mDeviceOrientation)) + 360) % 360;
        } else {  // back-facing camera
            captureRotation = (captureRotation + (mDisplayOrientation - mDeviceOrientation) + 360) % 360;
        }

        return captureRotation;
    }

    private void notifyErrorListener(@NonNull final String name, @NonNull final String details) {
        if (mErrorListener == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mErrorListener.onEvent(name, details);
            }
        });
    }

    private void notifyErrorListener(@NonNull final Exception e) {
        if (mErrorListener == null) {
            return;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mErrorListener.onError(e);
            }
        });
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
        boolean invertPreviewSizes = (mCameraInfo.orientation + mDeviceOrientation) % 180 == 90;
        boolean haveToReadjust = false;
        Camera.Parameters resolutionLess = mCamera.getParameters();

        if (getPreviewResolution() != null) {
            mPreview.setPreviewParameters(
                    getPreviewResolution().getWidth(),
                    getPreviewResolution().getHeight(),
                    mCameraParameters.getPreviewFormat()
            );

            mCameraParameters.setPreviewSize(
                    invertPreviewSizes ? getPreviewResolution().getHeight() : getPreviewResolution().getWidth(),
                    invertPreviewSizes ? getPreviewResolution().getWidth() : getPreviewResolution().getHeight()
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
        mCameraParameters.setRotation(rotation);

        setFocus(mFocus);

        try {
            setFlash(mFlash);
        } catch (Exception e) {
            notifyErrorListener("setFlash", e.getLocalizedMessage());
        }

        mCamera.setParameters(mCameraParameters);

        if (haveToReadjust && currentTry < 100) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            notifyErrorListener("retryAdjustParam", "Failed, try: " + currentTry);
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
            if (size.width >= CameraKit.Internal.screenHeight && size.height >= CameraKit.Internal.screenWidth) {
                previewAspectRatios.add(AspectRatio.of(size.width, size.height));
            }
        }

        Set<AspectRatio> captureAspectRatios = new HashSet<>();
        for (Camera.Size size : pictureSizes) {
            captureAspectRatios.add(AspectRatio.of(size.width, size.height));
        }

        TreeSet<AspectRatio> output = new TreeSet<>();
        for (AspectRatio aspectRatio : previewAspectRatios) {
            if (captureAspectRatios.contains(aspectRatio)) {
                output.add(aspectRatio);
            }
        }

        return output;
    }

    private void initMediaRecorder() {
        synchronized (mCameraLock) {
            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();

            mMediaRecorder.setCamera(mCamera);

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mMediaRecorder.setProfile(getCamcorderProfile(mVideoQuality));

            mVideoFile = new File(mPreview.getView().getContext().getFilesDir(), "video.mp4");
            mMediaRecorder.setOutputFile(mVideoFile.getAbsolutePath());
            mMediaRecorder.setOrientationHint(calculateCaptureRotation());

            Size videoSize = getVideoResolution();
            mMediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        }
    }

    private void prepareMediaRecorder() {
        synchronized (mCameraLock) {
            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private CamcorderProfile getCamcorderProfile(@VideoQuality int videoQuality) {
        CamcorderProfile camcorderProfile = null;
        switch (videoQuality) {
            case CameraKit.Constants.VIDEO_QUALITY_QVGA:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_QVGA)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_QVGA);
                } else {
                    return getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_LOWEST);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_480P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    return getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_QVGA);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_720P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_720P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_720P);
                } else {
                    return getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_480P);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_1080P:
                if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_1080P);
                } else {
                    return getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_720P);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_2160P:
                try {
                    camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_2160P);
                } catch (Exception e) {
                    return getCamcorderProfile(CameraKit.Constants.VIDEO_QUALITY_HIGHEST);
                }
                break;

            case CameraKit.Constants.VIDEO_QUALITY_HIGHEST:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_HIGH);
                break;

            case CameraKit.Constants.VIDEO_QUALITY_LOWEST:
                camcorderProfile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
                break;
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
