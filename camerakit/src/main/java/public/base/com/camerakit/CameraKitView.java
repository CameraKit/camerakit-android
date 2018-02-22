package com.camerakit;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * CameraKitView provides a high-level, easy to implement, and safe to use way to work with
 * the Android camera.
 *
 * @since v1.0.0
 */
public class CameraKitView extends CameraLayout {

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
     * Flash will activate during a photo capture's shutter.
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
     * Flash is constantly activated when the preview is showing.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_TORCH}
     * Camera2: {@link android.hardware.camera2.CameraCharacteristics#FLASH_MODE_TORCH}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_TORCH = 2;

    /**
     * Flash will activate during a photo capture's shutter, if needed.
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FLASH_MODE_AUTO}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AE_MODE_ON_AUTO_FLASH}
     *
     * @see #setFlash(int)
     * @see #getFlash()
     */
    public static final int FLASH_AUTO = 3;

    /**
     * Describes how the camera's flash should behave.
     * <p>
     * Use declared constants with {@link #setFlash(int)}.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO})
    @interface Flash {}

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_AUTO}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_AUTO}
     *
     * @see #setActiveFocusMode(int)
     * @see #getActiveFocusMode()
     */
    public static final int ACTIVE_FOCUS_MODE_OFF = 0;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_CONTINUOUS_PICTURE}
     *
     * @see #setActiveFocusMode(int)
     * @see #getActiveFocusMode()
     */
    public static final int ACTIVE_FOCUS_MODE_FAST = 1;

    /**
     * <p>
     * Related low-level constants:
     * Camera1: {@link android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_VIDEO}
     * Camera2: {@link android.hardware.camera2.CaptureRequest#CONTROL_AF_MODE_CONTINUOUS_VIDEO}
     *
     * @see #setActiveFocusMode(int)
     * @see #getActiveFocusMode()
     */
    public static final int ACTIVE_FOCUS_MODE_SMOOTH = 2;

    /**
     * Describes the constant in-the-background focus strategy for when autoFocus isn't manually
     * triggered.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTIVE_FOCUS_MODE_OFF, ACTIVE_FOCUS_MODE_FAST, ACTIVE_FOCUS_MODE_SMOOTH})
    @interface ActiveFocusMode {}

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

    /**
     * Flag for handling requesting the {@link android.Manifest.permission#CAMERA}
     * permission.
     */
    public static final int PERMISSION_CAMERA = 1;

    /**
     * Flag for handling requesting the {@link android.Manifest.permission#RECORD_AUDIO}
     * permission.
     */
    public static final int PERMISSION_MICROPHONE = 1 << 1;

    /**
     * Flag for handling requesting the {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
     * permission.
     */
    public static final int PERMISSION_STORAGE = 1 << 2;

    /**
     * Represents manifest runtime-permissions that may be used.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PERMISSION_CAMERA, PERMISSION_MICROPHONE, PERMISSION_STORAGE})
    @interface Permission {}

    /**
     *
     */
    public interface ErrorListener {

        /**
         * @param view
         * @param error
         */
        void onError(CameraKitView view, CameraKitException error);

    }

    /**
     *
     */
    public interface GestureListener {

        /**
         * @param view
         * @param x
         * @param y
         */
        void onTap(CameraKitView view, float x, float y);

        /**
         * @param view
         * @param x
         * @param y
         */
        void onLongTap(CameraKitView view, float x, float y);

        /**
         * @param view
         * @param x
         * @param y
         */
        void onDoubleTap(CameraKitView view, float x, float y);

        /**
         * @param view
         * @param ds
         * @param dsx
         * @param dsy
         */
        void onPinch(CameraKitView view, float ds, float dsx, float dsy);

    }

    /**
     *
     */
    public interface PermissionsCallback {

        /**
         * @param approvedPermissions
         */
        void onPermissionsApproved(@Permission int... approvedPermissions);

        /**
         * @param deniedPermissions
         */
        void onPermissionsDenied(@Permission int... deniedPermissions);

    }

    /**
     *
     */
    public interface PhotoCallback {

        /**
         * @param view
         * @param photo
         */
        void onPhoto(CameraKitView view, CameraKitPhoto photo);

    }

    /**
     *
     */
    public interface VideoCallback {

        /**
         * @param view
         * @param video
         */
        void onVideo(CameraKitView view, CameraKitVideo video);

    }

    private boolean mAdjustViewBounds;
    private float mAspectRatio;
    private int mFacing;
    private int mFlash;
    private int mActiveFocusMode;
    private float mZoomFactor;
    private boolean mPhotoHint;
    private boolean mVideoHint;
    private int mSensorPreset;
    private int mPreviewEffect;
    private GestureListener mGestureListener;

    private CameraApi mCameraApi;
    private CameraExecutor mCameraExecutor;
    private PreviewView mPreviewView;

    public CameraKitView(Context context) {
        super(context);
        obtainAttributes(context, null);
        initialize(context);
    }

    public CameraKitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainAttributes(context, attrs);
        initialize(context);
    }

    public CameraKitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttributes(context, attrs);
        initialize(context);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraKitView);

        mAdjustViewBounds = a.getBoolean(R.styleable.CameraKitView_android_adjustViewBounds, false);
        mAspectRatio = a.getFloat(R.styleable.CameraKitView_camera_aspectRatio, -1f);
        mFacing = a.getInteger(R.styleable.CameraKitView_camera_facing, FACING_BACK);
        mFlash = a.getInteger(R.styleable.CameraKitView_camera_flash, FLASH_OFF);
        mActiveFocusMode = a.getInteger(R.styleable.CameraKitView_camera_activeFocusMode, ACTIVE_FOCUS_MODE_FAST);
        mZoomFactor = a.getFloat(R.styleable.CameraKitView_camera_zoomFactor, 1.0f);
        mPhotoHint = a.getBoolean(R.styleable.CameraKitView_camera_photoHint, false);
        mVideoHint = a.getBoolean(R.styleable.CameraKitView_camera_videoHint, false);
//        mSensorPreset = a.getInteger(R.styleable.CameraKitView_camera_sensorPreset, SENSOR_PRESET_NONE);
//        mPreviewEffect = a.getInteger(R.styleable.CameraKitView_camera_previewEffect, PREVIEW_EFFECT_NONE);
//        mPermissions = a.getInteger(R.styleable.CameraKitView_camera_permissions, PERMISSION_CAMERA);

        a.recycle();
    }

    private void initialize(Context context) {
        mCameraExecutor = new CameraExecutor();
    }

    private CameraSize getPreviewSize() {
        if (mCameraApi == null) return null;
        if (mCameraApi.previewAttributes() == null) return null;
        if (mCameraApi.previewAttributes().supportedSizes() == null) return null;

        return mCameraApi.previewAttributes().supportedSizes().get(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdjustViewBounds) {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams.width == WRAP_CONTENT && layoutParams.height == WRAP_CONTENT) {
                throw new CameraException("android:adjustViewBounds=true while both layout_width and layout_height are setView to wrap_content - only 1 is allowed.");
            } else if (layoutParams.width == WRAP_CONTENT) {
                int width = 0;
                int height = MeasureSpec.getSize(heightMeasureSpec);

                if (mAspectRatio > 0) {
                    width = (int) (height * mAspectRatio);
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                } else if (getPreviewSize() != null) {
                    CameraSize previewSize = getPreviewSize();
                    if (getLastKnownDisplayOrientation() % 180 == 0) {
                        previewSize = previewSize.inverse();
                    }

                    width = (int) (((float) height / (float) previewSize.getHeight()) * previewSize.getWidth());
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                }
            } else if (layoutParams.height == WRAP_CONTENT) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = 0;

                if (mAspectRatio > 0) {
                    height = (int) (width * mAspectRatio);
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else if (getPreviewSize() != null) {
                    CameraSize previewSize = getPreviewSize();
                    if (getLastKnownDisplayOrientation() % 180 == 0) {
                        previewSize = previewSize.inverse();
                    }

                    height = (int) (((float) width / (float) previewSize.getWidth()) * previewSize.getHeight());
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onTap(float x, float y) {
        if (mGestureListener != null) {
            mGestureListener.onTap(this, x, y);
        }
    }

    @Override
    protected void onLongTap(float x, float y) {
        if (mGestureListener != null) {
            mGestureListener.onLongTap(this, x, y);
        }
    }

    @Override
    protected void onDoubleTap(float x, float y) {
        if (mGestureListener != null) {
            mGestureListener.onDoubleTap(this, x, y);
        }
    }

    @Override
    protected void onPinch(float ds, float dsx, float dsy) {
        if (mGestureListener != null) {
            mGestureListener.onPinch(this, ds, dsx, dsy);
        }
    }

    @Override
    void onOrientationChanged(int displayOrientation, int deviceOrientation) {
        if (mCameraApi != null) {
            mCameraApi.previewApi()
                    .setDisplayOrientation(displayOrientation);
        }
    }

    /**
     *
     */
    public void start() {
        if (isInEditMode()) {
            return;
        }

        if (mCameraApi != null) {
            return;
        }

        if (mPreviewView != null && indexOfChild(mPreviewView) >= 0) {
            removeView(mPreviewView);
            mPreviewView = null;
        }

        mCameraExecutor.start();

        CameraFacing facing = mFacing == FACING_BACK ? CameraFacing.BACK : CameraFacing.FRONT;
        mCameraApi = new Camera1(getContext(), mCameraExecutor, facing);

        mCameraApi.connect()
                .result(previewView -> {
                    mPreviewView = previewView;

                    post(() -> {
                        addView(mPreviewView);
                        mPreviewView.setSurfaceCallback(new PreviewView.SurfaceCallback() {
                            @Override
                            public void attachSurface(SurfaceHolder surfaceHolder) {
                                if (mCameraApi != null) {
                                    mCameraApi.previewApi()
                                            .stop()
                                            .then(() -> {
                                                CameraSize previewSize = getPreviewSize();
                                                mCameraApi.previewApi().setSize(previewSize.getWidth(), previewSize.getHeight());
                                                invalidate();
                                            })
                                            .then(() -> mCameraApi.previewApi().setSurface(surfaceHolder))
                                            .then(() -> mCameraApi.previewApi().start());
                                }
                            }

                            @Override
                            public void detachSurface() {
                                if (mCameraApi != null) {
                                    mCameraApi.previewApi()
                                            .stop();
                                }
                            }
                        });
                        enableOrientationDetection();
                    });
                })
                .error(error -> {
                    throw new CameraException("Failed to connect to Camera API.", error);
                });
    }

    /**
     *
     */
    public void stop() {
        if (isInEditMode()) {
            return;
        }

        disableOrientationDetection();

        if (mCameraApi != null) {
            mCameraApi.disconnect();
            mCameraApi = null;
        }

        if (mPreviewView != null) {
            mPreviewView.setSurfaceCallback(null);
        }

        mCameraExecutor.stop();
    }

    /**
     * @param permissions
     * @param callback
     * @see #PERMISSION_CAMERA
     * @see #PERMISSION_MICROPHONE
     * @see #PERMISSION_STORAGE
     */
    public void requestPermissions(@Permission int[] permissions, PermissionsCallback callback) {

    }

    /**
     * @param callback
     */
    public void capturePhoto(PhotoCallback callback) {
        List<CameraSize> sizes = mCameraApi.photoAttributes().supportedSizes();
        CameraSize size = sizes.get(0);

        mCameraApi.photoApi()
                .setSize(size.getWidth(), size.getHeight())
                .then(() -> {
                    mCameraApi.photoApi().setJpegQuality(100).then(() -> {
                        mCameraApi.photoApi()
                                .captureStandard()
                                .result((jpeg) -> {
                                    callback.onPhoto(this, new CameraKitPhoto(getContext(), jpeg));
                                    mCameraApi.previewApi()
                                            .start();
                                });
                    });
                });
    }

    /**
     * @param callback
     */
    public void captureVideo(VideoCallback callback) {

    }

    /**
     *
     */
    public void startRecording() {

    }

    /**
     *
     */
    public void stopRecording() {

    }

    /**
     * @param adjustViewBounds
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mAdjustViewBounds = adjustViewBounds;
    }

    /**
     * @return
     * @see #setAdjustViewBounds(boolean)
     */
    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    /**
     * @param aspectRatio
     */
    public void setAspectRatio(float aspectRatio) {
        this.mAspectRatio = aspectRatio;
    }

    /**
     * @return
     * @see #setAspectRatio(float)
     */
    public float getAspectRatio() {
        return mAspectRatio;
    }

    /**
     * @param facing one of {@link Facing}'s constants.
     * @see #FACING_BACK
     * @see #FACING_FRONT
     */
    public void setFacing(@Facing int facing) {
        mFacing = facing;
    }

    /**
     * @return one of {@link Facing}'s constants.
     * @see #setFacing(int)
     */
    @Facing
    public int getFacing() {
        return mFacing;
    }

    /**
     *
     */
    public void toggleFacing() {
        if (getFacing() == FACING_BACK) {
            setFacing(FACING_FRONT);
        } else {
            setFacing(FACING_BACK);
        }
    }

    /**
     * @param flash one of {@link Flash}'s constants.
     * @see #FLASH_OFF
     * @see #FLASH_ON
     * @see #FLASH_TORCH
     * @see #FLASH_AUTO
     */
    public void setFlash(@Flash int flash) {
        mFlash = flash;
    }

    /**
     * @return one of {@link Flash}'s constants.
     * @see #setFlash(int)
     */
    @Flash
    public int getFlash() {
        return mFlash;
    }

    /**
     * @param activeFocusMode one of {@link ActiveFocusMode}'s constants.
     * @see #ACTIVE_FOCUS_MODE_OFF
     * @see #ACTIVE_FOCUS_MODE_FAST
     * @see #ACTIVE_FOCUS_MODE_SMOOTH
     */
    public void setActiveFocusMode(@ActiveFocusMode int activeFocusMode) {
        mActiveFocusMode = activeFocusMode;
    }

    /**
     * @return one of {@link ActiveFocusMode}'s constants.
     * @see #setActiveFocusMode(int)
     */
    @ActiveFocusMode
    public int getActiveFocusMode() {
        return mActiveFocusMode;
    }

    /**
     * @param zoomFactor
     */
    public void setZoomFactor(float zoomFactor) {
        mZoomFactor = zoomFactor;
    }

    /**
     * @return
     * @see #setZoomFactor(float)
     */
    public float getZoomFactor() {
        return mZoomFactor;
    }

    /**
     * @param photoHint
     */
    public void setPhotoHint(boolean photoHint) {
        mPhotoHint = photoHint;
    }

    /**
     * @return
     * @see #setPhotoHint(boolean)
     */
    public boolean getPhotoHint() {
        return mPhotoHint;
    }

    /**
     * @param videoHint
     */
    public void setVideoHint(boolean videoHint) {
        mVideoHint = videoHint;
    }

    /**
     * @return
     * @see #setVideoHint(boolean)
     */
    public boolean getVideoHint() {
        return mVideoHint;
    }

    /**
     * @param sensorPreset one of {@link SensorPreset}'s constants.
     * @see #SENSOR_PRESET_NONE
     * @see #SENSOR_PRESET_ACTION
     * @see #SENSOR_PRESET_PORTRAIT
     * @see #SENSOR_PRESET_LANDSCAPE
     * @see #SENSOR_PRESET_NIGHT
     * @see #SENSOR_PRESET_NIGHT_PORTRAIT
     * @see #SENSOR_PRESET_THEATRE
     * @see #SENSOR_PRESET_BEACH
     * @see #SENSOR_PRESET_SNOW
     * @see #SENSOR_PRESET_SUNSET
     * @see #SENSOR_PRESET_STEADYPHOTO
     * @see #SENSOR_PRESET_FIREWORKS
     * @see #SENSOR_PRESET_SPORTS
     * @see #SENSOR_PRESET_PARTY
     * @see #SENSOR_PRESET_CANDLELIGHT
     * @see #SENSOR_PRESET_BARCODE
     */
    public void setSensorPreset(@SensorPreset int sensorPreset) {
        mSensorPreset = sensorPreset;
    }

    /**
     * @return one of {@link SensorPreset}'s constants.
     * @see #setSensorPreset(int)
     */
    @SensorPreset
    public int getSensorPreset() {
        return mSensorPreset;
    }

    /**
     * @param previewEffect one of {@link PreviewEffect}'s constants.
     * @see #PREVIEW_EFFECT_NONE
     * @see #PREVIEW_EFFECT_MONO
     * @see #PREVIEW_EFFECT_SOLARIZE
     * @see #PREVIEW_EFFECT_SEPIA
     * @see #PREVIEW_EFFECT_POSTERIZE
     * @see #PREVIEW_EFFECT_WHITEBOARD
     * @see #PREVIEW_EFFECT_BLACKBOARD
     * @see #PREVIEW_EFFECT_AQUA
     */
    public void setPreviewEffect(@PreviewEffect int previewEffect) {
        mPreviewEffect = previewEffect;
    }

    /**
     * @return one of {@link PreviewEffect}'s constants.
     * @see #setPreviewEffect(int)
     */
    @PreviewEffect
    public int getPreviewEffect() {
        return mPreviewEffect;
    }

    /**
     * @param gestureListener
     */
    public void setGestureListener(GestureListener gestureListener) {
        mGestureListener = gestureListener;
    }

    /**
     * @return
     * @see #setGestureListener(GestureListener)
     */
    public GestureListener getGestureListener() {
        return mGestureListener;
    }

    /**
     * This adapter class provides empty implementations of the methods from {@link GestureListener}.
     * Any custom listener that cares only about a subset of the methods of this listener can
     * simply subclass this adapter class instead of implementing the interface directly.
     */
    public static class GestureListenerAdapter implements GestureListener {

        /**
         * @see GestureListener#onTap(CameraKitView, float, float)
         */
        @Override
        public void onTap(CameraKitView view, float x, float y) {
        }

        /**
         * @see GestureListener#onLongTap(CameraKitView, float, float)
         */
        @Override
        public void onLongTap(CameraKitView view, float x, float y) {
        }

        /**
         * @see GestureListener#onDoubleTap(CameraKitView, float, float)
         */
        @Override
        public void onDoubleTap(CameraKitView view, float x, float y) {
        }

        /**
         * @see GestureListener#onPinch(CameraKitView, float, float, float)
         */
        @Override
        public void onPinch(CameraKitView view, float ds, float dsx, float dsy) {
        }

    }

}
