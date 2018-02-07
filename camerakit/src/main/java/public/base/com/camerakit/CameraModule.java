package com.camerakit;

public class CameraModule {

    protected CameraView mCameraView;
    protected CameraApi mCameraApi;

    void setView(CameraView cameraView) {
        mCameraView = cameraView;
    }

    void setApi(CameraApi cameraApi) {
        mCameraApi = cameraApi;
    }

    protected boolean isFacingFront() {
        if (mCameraView != null) {
            return mCameraView.getFacing() == CameraFacing.FRONT;
        }

        return false;
    }

    protected boolean isFacingBack() {
        if (mCameraView != null) {
            return mCameraView.getFacing() == CameraFacing.BACK;
        }

        return false;
    }

}
