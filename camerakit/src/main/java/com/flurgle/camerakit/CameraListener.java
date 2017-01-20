package com.flurgle.camerakit;

import java.io.File;

public abstract class CameraListener {

    public void onCameraOpened() {

    }

    public void onCameraClosed() {

    }

    public void onPictureTaken(File picture) {

    }

    public void onPictureTaken(byte[] picture) {

    }

    public void onVideoTaken(File video) {

    }

}