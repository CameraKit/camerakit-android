package com.flurgle.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.FOCUS_TAP;
import static com.flurgle.camerakit.CameraKit.Constants.FOCUS_OFF;
import static com.flurgle.camerakit.CameraKit.Constants.FOCUS_CONTINUOUS;

@Retention(RetentionPolicy.SOURCE)
@IntDef({FOCUS_CONTINUOUS, FOCUS_TAP, FOCUS_OFF})
public @interface Focus {
}