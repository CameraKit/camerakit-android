package com.flurgle.camerakit;

abstract class CameraImpl {

    protected final CameraListener mCameraListener;
    protected final PreviewImpl mPreview;
    protected Config mConfig = new Config.Builder().build(); //Build a default config, so we have default values and this object is never null

    CameraImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    public void setConfig(Config config) {
        if (config != null) {
            this.mConfig = config;
        } else {
            throw new NullPointerException("Config can not be null. If no config is set, a default one will be used");
        }
    }

    abstract void start();
    abstract void stop();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract void setFacing(@Facing int facing);
    abstract void setFlash(@Flash int flash);
    abstract void setFocus(@Focus int focus);
    abstract void setMethod(@Method int method);
    abstract void setZoom(@Zoom int zoom);
    abstract void setVideoQuality(@VideoQuality int videoQuality);

    abstract void captureImage();
    abstract void startVideo();
    abstract void endVideo();

    abstract Size getCaptureResolution();
    abstract Size getPreviewResolution();
    abstract boolean isCameraOpened();

}
