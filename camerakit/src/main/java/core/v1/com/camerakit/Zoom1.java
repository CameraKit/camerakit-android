package com.camerakit;

import android.hardware.Camera;

import java.util.List;

class Zoom1 extends Camera1.BaseApi implements CameraApi.ZoomApi, CameraAttributes.ZoomAttributes {

    Zoom1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    @Override
    public CameraFuture zoom(float factor) {
        return new CameraFuture(mCameraExecutor, () -> {
            if (!canZoom()) {
                return;
            }

            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setZoom();
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture smoothZoom(float factor) {
        return new CameraFuture(mCameraExecutor, () -> {
            if (!canZoom()) {
                return;
            }

//            mCamera.startSmoothZoom();
        });
    }

    // *** Attributes

    @Override
    public boolean canZoom() {
        return mCameraParameters.isZoomSupported();
    }

    @Override
    public boolean canSmoothZoom() {
        return mCameraParameters.isSmoothZoomSupported();
    }

    @Override
    public float maxZoom() {
        if (!canZoom()) {
            return 1f;
        }

        List<Integer> zooms = mCameraParameters.getZoomRatios();
        if (zooms.size() > 0) {
            int maxZoom = zooms.get(zooms.size() - 1);
            return ((float) maxZoom) / 100f;
        }

        return 1f;
    }

    @Override
    public float[] supportedZooms() {
        if (!canZoom()) {
            return new float[0];
        }

        List<Integer> zooms = mCameraParameters.getZoomRatios();
        float[] output = new float[zooms.size()];

        for (int i = 0; i < output.length; i++) {
            output[i] = ((float) zooms.get(i)) / 100f;
        }

        return output;
    }

}
