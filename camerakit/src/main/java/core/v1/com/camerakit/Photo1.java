package com.camerakit;

import android.hardware.Camera;

import java.util.List;

class Photo1 extends Camera1.BaseApi implements CameraApi.PhotoApi, CameraAttributes.PhotoAttributes {

    Photo1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    @Override
    public CameraFuture setSize(int width, int height) {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(width, height);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture setJpegQuality(int jpegQuality) {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setJpegQuality(jpegQuality);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture<byte[]> captureStandard() {
        return new CameraFuture<>(mCameraExecutor, (cameraFuture -> {
            mCamera.takePicture(null, null, (data, camera) -> {
                cameraFuture.complete(data);
            });
        }));
    }

    @Override
    public CameraFuture<byte[]> capturePreview() {
        return new CameraFuture<>(mCameraExecutor, (cameraFuture -> {
            mCamera.setOneShotPreviewCallback((data, camera) -> {
                cameraFuture.complete(data);
            });
        }));
    }

    // *** Attributes

    @Override
    public List<CameraSize> supportedSizes() {
        return convertSizeList(mCameraParameters.getSupportedPictureSizes());
    }

    @Override
    public boolean canDisableShutterSound() {
        return mCameraInfo.canDisableShutterSound;
    }

}