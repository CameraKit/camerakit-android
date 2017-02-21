package com.flurgle.camerakit;

import android.graphics.YuvImage;
import android.hardware.Camera;

class ProcessStillTask implements Runnable {

    private byte[] data;
    private Camera camera;
    private Camera.CameraInfo cameraInfo;
    private OnStillProcessedListener onStillProcessedListener;

    public ProcessStillTask(byte[] data, Camera camera, Camera.CameraInfo cameraInfo, OnStillProcessedListener onStillProcessedListener) {
        this.data = data;
        this.camera = camera;
        this.cameraInfo = cameraInfo;
        this.onStillProcessedListener = onStillProcessedListener;
    }

    @Override
    public void run() {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        int rotation = cameraInfo.orientation;
        byte[] rotatedData = new Rotation(data, width, height, rotation).getYuv();

        int postWidth;
        int postHeight;

        switch (rotation) {
            case 90:
            case 270:
                postWidth = height;
                postHeight = width;
                break;

            case 0:
            case 180:
            default:
                postWidth = width;
                postHeight = height;
                break;
        }

        YuvImage yuv = new YuvImage(rotatedData, parameters.getPreviewFormat(), postWidth, postHeight, null);

        onStillProcessedListener.onStillProcessed(yuv);
    }

    interface OnStillProcessedListener {
        void onStillProcessed(YuvImage yuv);
    }

}