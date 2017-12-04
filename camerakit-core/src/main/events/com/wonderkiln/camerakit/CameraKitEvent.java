package com.wonderkiln.camerakit;

import android.os.Bundle;
import android.support.annotation.NonNull;

public class CameraKitEvent {

    public static final String TYPE_ERROR = "CameraKitError";

    public static final String TYPE_CAMERA_OPEN = "CKCameraOpenedEvent";
    public static final String TYPE_CAMERA_CLOSE = "CKCameraStoppedEvent";

    public static final String TYPE_FACING_CHANGED = "CKFacingChangedEvent";
    public static final String TYPE_FLASH_CHANGED = "CKFlashChangedEvent";

    public static final String TYPE_IMAGE_CAPTURED = "CKImageCapturedEvent";
    public static final String TYPE_VIDEO_CAPTURED = "CKVideoCapturedEvent";

    public static final String TYPE_FOCUS_MOVED = "CKFocusMovedEvent";

    public static final String TYPE_TEXT_DETECTED = "CKTextDetectedEvent";

    private String type;
    private String message;

    private Bundle data;

    private CameraKitEvent() {
    }

    CameraKitEvent(@NonNull String type) {
        this.type = type;
        data = new Bundle();
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getMessage() {
        if (message != null) {
            return message;
        }

        return "";
    }

    @NonNull
    public Bundle getData() {
        if (data != null) {
            return data;
        }

        return new Bundle();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getType(), getMessage());
    }

}