package com.flurgle.camerakit;

import android.graphics.YuvImage;

import java.io.File;

public abstract class CameraListener {

    public void onCameraOpened() {

    }

    public void onCameraClosed() {

    }

    public void onCameraError(Throwable throwable) {

    }

    public void onPictureTaken(byte[] jpeg) {

    }

    public void onPictureTaken(YuvImage yuv) {

    }

    public void onVideoTaken(File video) {

    }

}