package com.camerakit;

public class CameraZoom extends CameraModule {

    public void resetZoom() {
        zoomTo(1f);
    }

    public void zoomTo(float factor) {

    }

    public void smoothZoomTo(float factor) {

    }

    protected CameraApi.ZoomApi zoomApi() {
        if (mCameraApi != null) {
            return mCameraApi.zoomApi();
        }

        return null;
    }

    protected CameraAttributes.ZoomAttributes zoomAttributes() {
        if (mCameraApi != null) {
            return mCameraApi.zoomAttributes();
        }

        return null;
    }

}
