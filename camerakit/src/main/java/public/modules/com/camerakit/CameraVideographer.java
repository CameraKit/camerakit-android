package com.camerakit;

public class CameraVideographer extends CameraModule {

    protected CameraApi.VideoApi videoApi() {
        if (mCameraApi != null) {
            return mCameraApi.videoApi();
        }

        return null;
    }

    protected CameraAttributes.VideoAttributes videoAttributes() {
        if (mCameraApi != null) {
            return mCameraApi.videoAttributes();
        }

        return null;
    }
    
}
