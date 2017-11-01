package com.wonderkiln.camerakit;

import android.support.annotation.Nullable;

public class CKError extends CKEvent {

    private String type;
    private String message;
    private Exception exception;

    CKError() {
        super(TYPE_ERROR);
    }

    CKError(Exception exception) {
        super(TYPE_ERROR);
        this.exception = exception;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

}