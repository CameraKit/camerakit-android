package com.camerakit;

import android.hardware.Camera;

import java.util.List;

class Video1 extends Camera1.BaseApi implements CameraApi.VideoApi, CameraAttributes.VideoAttributes {

    Video1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    // *** Attributes

    @Override
    public boolean canRecordVideo() {
        return mCameraParameters.getSupportedVideoSizes() != null
                && mCameraParameters.getSupportedVideoSizes().size() > 0;
    }

    @Override
    public CameraSize preferredSize() {
        Camera.Size size = mCameraParameters.getPreferredPreviewSizeForVideo();
        return new CameraSize(size.width, size.height);
    }

    @Override
    public List<CameraSize> supportedSizes() {
        return convertSizeList(mCameraParameters.getSupportedVideoSizes());
    }

}