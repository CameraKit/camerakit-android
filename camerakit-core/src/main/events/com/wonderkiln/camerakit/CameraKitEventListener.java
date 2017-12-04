package com.wonderkiln.camerakit;

public interface CameraKitEventListener {
    void onEvent(CameraKitEvent event);
    void onError(CameraKitError error);
    void onImage(CameraKitImage image);
    void onVideo(CameraKitVideo video);
}