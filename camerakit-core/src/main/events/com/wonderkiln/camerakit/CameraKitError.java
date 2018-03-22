package com.wonderkiln.camerakit;

import android.support.annotation.Nullable;

public class CameraKitError extends CameraKitEvent {

    private Exception exception;

    CameraKitError() {
        super(TYPE_ERROR);
    }

    CameraKitError(Exception exception) {
        super(TYPE_ERROR);
        this.exception = exception;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

}
