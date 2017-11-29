package com.wonderkiln.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.ZOOM_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.ZOOM_PINCH;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ZOOM_OFF, ZOOM_PINCH})
public @interface Zoom {
}
