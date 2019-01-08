package com.wonderkiln.camerakit;

import android.graphics.YuvImage;
import android.hardware.Camera;

class ProcessStillTask implements Runnable {

    private byte[] data;
    private Camera camera;
    private int rotation;
    private OnStillProcessedListener onStillProcessedListener;

    public ProcessStillTask(byte[] data, Camera camera, int rotation, OnStillProcessedListener onStillProcessedListener) {
        this.data = data;
        this.camera = camera;
        this.rotation = rotation;
        this.onStillProcessedListener = onStillProcessedListener;
    }

    @Override
    public void run() {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
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