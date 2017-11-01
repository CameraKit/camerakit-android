package com.wonderkiln.camerakit;

public interface CKEventCallback<T extends CKEvent> {
    void callback(T event);
}
