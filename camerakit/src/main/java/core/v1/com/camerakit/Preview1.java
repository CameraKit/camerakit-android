package com.camerakit;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.util.List;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

class Preview1 extends Camera1.BaseApi implements CameraApi.PreviewApi, CameraAttributes.PreviewAttributes {

    Preview1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    @Override
    public CameraFuture setDisplayOrientation(int displayOrientation) {
        return new CameraFuture(mCameraExecutor, () -> {
            final int previewRotation;
            if (mCameraInfo.facing == CAMERA_FACING_FRONT) {
                previewRotation = (360 - ((mCameraInfo.orientation + displayOrientation) % 360)) % 360;
            } else {
                previewRotation = (mCameraInfo.orientation - displayOrientation + 360) % 360;
            }

            mCamera.setDisplayOrientation(previewRotation);
        });
    }

    @Override
    public CameraFuture setSize(int width, int height) {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(width, height);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture setSurface(SurfaceHolder surfaceHolder) {
        return new CameraFuture(mCameraExecutor, () -> {
            mCamera.setPreviewDisplay(surfaceHolder);
        });
    }

    @Override
    public CameraFuture start() {
        return new CameraFuture(mCameraExecutor, () -> {
            mCamera.startPreview();
        });
    }

    @Override
    public CameraFuture stop() {
        return new CameraFuture(mCameraExecutor, () -> {
            mCamera.stopPreview();
        });
    }

    // *** Attributes

    @Override
    public List<CameraSize> supportedSizes() {
        if (mCameraParameters == null) {
            return null;
        }

        return convertSizeList(mCameraParameters.getSupportedPreviewSizes());
    }

}