package com.camerakit;

public class CameraFocus extends CameraModule {

    protected CameraApi.FocusApi focusApi() {
        if (mCameraApi != null) {
            return mCameraApi.focusApi();
        }

        return null;
    }

    protected CameraAttributes.FocusAttributes focusAttributes() {
        if (mCameraApi != null) {
            return mCameraApi.focusAttributes();
        }

        return null;
    }
    
}
