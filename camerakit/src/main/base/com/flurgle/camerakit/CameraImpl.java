package com.flurgle.camerakit;

abstract class CameraImpl {

    protected final CameraListener mCameraListener;
    protected final PreviewImpl mPreview;

    CameraImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    abstract void start();
    abstract void stop();

    abstract void setFacing(@Facing int facing);
    abstract void setFlash(@Flash int flash);
    abstract void setContinuousFocus(boolean autoFocus);

    abstract void setDisplayOrientation(int displayOrientation);
    abstract void focus();
    abstract void zoom(float percentZoom);

    abstract void captureStandard();
    abstract void captureStill();

    abstract void startVideo();
    abstract void endVideo();

    abstract Size getCaptureResolution();
    abstract Size getPreviewResolution();
    abstract boolean isCameraOpened();

}
