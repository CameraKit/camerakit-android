package com.wonderkiln.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK;
import static com.wonderkiln.camerakit.CameraKit.Constants.FACING_FRONT;

@IntDef({FACING_BACK, FACING_FRONT})
@Retention(RetentionPolicy.SOURCE)
public @interface Facing {
}