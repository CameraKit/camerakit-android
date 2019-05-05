package com.camerakit;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
public class CameraKit {

    /**
     * The device points away from the screen.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#LENS_FACING_BACK}
     *
     * @see #setFacing(int)
     * @see #getFacing()
     */
    public static final int FACING_BACK = 0;

    /**
     * The device points in the same direction as the screen.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#LENS_FACING_FRONT}
     *
     * @see #setFacing(int)
     * @see #getFacing()
     */
    public static final int FACING_FRONT = 1;

    /**
     * Describes the orientation of the camera lens relative to the screen.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FACING_BACK, FACING_FRONT})
    @interface Facing {}

    /**
     * Flash will never activate.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_OFF}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#FLASH_MODE_OFF}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_OFF = 0;

    /**
     * Flash will activate during a image capture's shutter.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_ON}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#FLASH_MODE_SINGLE}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_ON = 1;

    /**
     * Flash will activate during a image capture's shutter, if needed.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_AUTO}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AE_MODE_ON_AUTO_FLASH}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_AUTO = 2;


    /**
     * Flash is constantly activated when the preview is showing.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_TORCH}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#FLASH_MODE_TORCH}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_TORCH = 3;

    /**
     * Describes how the camera's flash should behave.
     * <p>
     * Use declared constants with {@link #setFlash(int)}.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_AUTO, FLASH_TORCH})
    @interface Flash {}

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_FIXED}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_OFF}
     *
     * @see #setFocus(int)
     * @see #getFocus()
     */
    public static final int FOCUS_OFF = 0;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_AUTO}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_AUTO}
     *
     * @see #setFocus(int)
     * @see #getFocus()
     */
    public static final int FOCUS_AUTO = 1;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_CONTINUOUS_PICTURE}
     *
     * @see #setFocus(int)
     * @see #getFocus()
     */
    public static final int FOCUS_CONTINUOUS = 2;

    /**
     * Describes the constant in-the-background focus strategy for when autoFocus isn't manually
     * triggered.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FOCUS_OFF, FOCUS_AUTO, FOCUS_CONTINUOUS})
    @interface Focus {}

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_BARCODE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_BARCODE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_NONE = 0;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_BARCODE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_BARCODE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_ACTION = 1;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_BARCODE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_BARCODE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_PORTRAIT = 2;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_LANDSCAPE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_LANDSCAPE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_LANDSCAPE = 3;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_NIGHT}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_NIGHT}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_NIGHT = 4;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_NIGHT_PORTRAIT}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_NIGHT_PORTRAIT}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_NIGHT_PORTRAIT = 5;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_THEATRE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_THEATRE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_THEATRE = 6;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_BEACH}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_BEACH}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_BEACH = 7;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_SNOW}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_SNOW}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_SNOW = 8;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_SUNSET}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_SUNSET}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_SUNSET = 9;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_STEADYPHOTO}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_STEADYPHOTO}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_STEADYPHOTO = 10;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_FIREWORKS}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_FIREWORKS}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_FIREWORKS = 11;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_SPORTS}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_SPORTS}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_SPORTS = 12;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_PARTY}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_PARTY}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_PARTY = 13;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_CANDLELIGHT}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_CANDLELIGHT}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_CANDLELIGHT = 14;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#SCENE_MODE_BARCODE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_SCENE_MODE_BARCODE}
     *
     * @see #setSensorPreset(int)
     * @see #getSensorPreset()
     */
    public static final int SENSOR_PRESET_BARCODE = 15;

    /**
     * Describes the constant in-the-background focus strategy for when autoFocus isn't manually
     * triggered.
     * <p>
     * Use declared constants with {@link #setSensorPreset(int)}.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SENSOR_PRESET_NONE, SENSOR_PRESET_ACTION, SENSOR_PRESET_PORTRAIT,
            SENSOR_PRESET_LANDSCAPE, SENSOR_PRESET_NIGHT, SENSOR_PRESET_NIGHT_PORTRAIT,
            SENSOR_PRESET_THEATRE, SENSOR_PRESET_BEACH, SENSOR_PRESET_SNOW, SENSOR_PRESET_SUNSET,
            SENSOR_PRESET_STEADYPHOTO, SENSOR_PRESET_FIREWORKS, SENSOR_PRESET_SPORTS,
            SENSOR_PRESET_PARTY, SENSOR_PRESET_CANDLELIGHT, SENSOR_PRESET_BARCODE})
    @interface SensorPreset {}

    /**
     * No effect will be applied to the preview.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_NONE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_OFF}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_NONE = 0;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_MONO}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_MONO}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_MONO = 1;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_NEGATIVE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_NEGATIVE}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_NEGATIVE = 2;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_SOLARIZE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_SOLARIZE}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_SOLARIZE = 3;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_SEPIA}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_SEPIA}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_SEPIA = 4;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_POSTERIZE}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_POSTERIZE}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_POSTERIZE = 5;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_WHITEBOARD}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_WHITEBOARD}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_WHITEBOARD = 6;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_BLACKBOARD}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_BLACKBOARD}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_BLACKBOARD = 7;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#EFFECT_AQUA}
     * Camera2: {@link android.hardware.camera2.CameraMetadata#CONTROL_EFFECT_MODE_AQUA}
     *
     * @see #setPreviewEffect(int)
     * @see #getPreviewEffect()
     */
    public static final int PREVIEW_EFFECT_AQUA = 8;

    /**
     * Describes the constant in-the-background focus strategy for when auto focus isn't manually
     * triggered.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PREVIEW_EFFECT_NONE, PREVIEW_EFFECT_MONO, PREVIEW_EFFECT_NEGATIVE,
            PREVIEW_EFFECT_SOLARIZE, PREVIEW_EFFECT_SEPIA, PREVIEW_EFFECT_POSTERIZE,
            PREVIEW_EFFECT_WHITEBOARD, PREVIEW_EFFECT_BLACKBOARD, PREVIEW_EFFECT_AQUA})
    @interface PreviewEffect {}

}
