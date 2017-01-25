package com.flurgle.camerakit.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.PICTURE_MODE_QUALITY;
import static com.flurgle.camerakit.CameraKit.Constants.PICTURE_MODE_SPEED;

@Retention(RetentionPolicy.SOURCE)
@IntDef({PICTURE_MODE_QUALITY, PICTURE_MODE_SPEED})
public @interface PictureMode {
}
