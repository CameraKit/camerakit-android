package com.flurgle.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_INVISIBLE;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_OFF;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_VISIBLE;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TAP_TO_FOCUS_VISIBLE, TAP_TO_FOCUS_INVISIBLE, TAP_TO_FOCUS_OFF})
public @interface TapToFocus {
}