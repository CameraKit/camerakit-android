package com.wonderkiln.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STANDARD;
import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STILL;

@Retention(RetentionPolicy.SOURCE)
@IntDef({METHOD_STANDARD, METHOD_STILL})
public @interface CaptureMethod {
}
