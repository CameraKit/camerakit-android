package com.wonderkiln.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_TAP;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_CONTINUOUS;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_TAP_WITH_MARKER;

@Retention(RetentionPolicy.SOURCE)
@IntDef({FOCUS_CONTINUOUS, FOCUS_TAP, FOCUS_OFF, FOCUS_TAP_WITH_MARKER})
public @interface Focus {
}