package com.wonderkiln.camerakit;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.wonderkiln.camerakit.CameraKit.Constants.PERMISSIONS_LAZY;
import static com.wonderkiln.camerakit.CameraKit.Constants.PERMISSIONS_PICTURE;
import static com.wonderkiln.camerakit.CameraKit.Constants.PERMISSIONS_STRICT;

@Retention(RetentionPolicy.SOURCE)
@IntDef({PERMISSIONS_STRICT, PERMISSIONS_LAZY, PERMISSIONS_PICTURE})
public @interface Permissions {
}
