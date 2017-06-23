package com.flurgle.camerakit;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.AUDIO_DEFAULT;
import static com.flurgle.camerakit.CameraKit.Constants.AUDIO_MUTED;
import static com.flurgle.camerakit.CameraKit.Constants.AUDIO_VOICE_RECOGNITION_COMPATIBLE;


/**
 * Created by yatsinar on 23/06/2017.
 */
@IntDef({AUDIO_DEFAULT, AUDIO_VOICE_RECOGNITION_COMPATIBLE, AUDIO_MUTED})
@Retention(RetentionPolicy.SOURCE)
public @interface Audio {

}
