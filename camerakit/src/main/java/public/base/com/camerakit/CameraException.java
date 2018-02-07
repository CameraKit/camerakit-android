package com.camerakit;

public class CameraException extends RuntimeException {

    private CameraException() {
    }

    public CameraException(String message) {
        super(message);
    }

    public CameraException(String message, Throwable cause) {
        super(message, cause);
    }

}
