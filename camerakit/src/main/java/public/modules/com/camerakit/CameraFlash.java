package com.camerakit;

import android.content.pm.PackageManager;

public class CameraFlash extends CameraModule {

    public boolean isFlashAvailable() {
        if (isFacingBack() && mCameraView != null) {
            return mCameraView.getContext()
                    .getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        } else if (isFacingFront()) {
            return true;
        }

        return false;
    }

    public void turnOff() {
        if (flashApi() != null) {
            flashApi().off();
        }
    }

    public void turnOn() {
        if (flashApi() != null) {
            flashApi().torch();
        }
    }

    protected CameraApi.FlashApi flashApi() {
        if (mCameraApi != null) {
            return mCameraApi.flashApi();
        }

        return null;
    }

    protected CameraAttributes.FlashAttributes flashAttributes() {
        if (mCameraApi != null) {
            return mCameraApi.flashAttributes();
        }

        return null;
    }

}
