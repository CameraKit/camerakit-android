package com.wonderkiln.camerakit;

import android.view.View;

abstract class CameraViewImpl {

    protected CameraListener mCameraListener;

    protected final PreviewImpl mPreview;

    CameraViewImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    abstract void start();

    abstract void stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(int facing);

    abstract int getFacing();

    abstract void setFlash(int flash);

    abstract int getFlash();

    abstract void capturePicture();

    abstract void captureStill();

    abstract void startVideo();

    abstract void endVideo();

    abstract void setDisplayOrientation(int displayOrientation);

    void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener = cameraListener;
    }

    protected CameraListener getCameraListener() {
        return mCameraListener != null ? mCameraListener : new CameraListener() {};
    }

}
