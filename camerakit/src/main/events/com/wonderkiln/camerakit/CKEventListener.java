package com.wonderkiln.camerakit;

public interface CKEventListener {
    void onEvent(CKEvent event);
    void onError(CKError error);
    void onImage(CKImage image);
    void onVideo(CKVideo video);
}