package com.flurgle.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.CAPTURE_MODE_STANDARD;
import static com.flurgle.camerakit.CameraKit.Constants.CAPTURE_MODE_STILL;

@Retention(RetentionPolicy.SOURCE)
@IntDef({CAPTURE_MODE_STANDARD, CAPTURE_MODE_STILL})
public @interface Method {
}
