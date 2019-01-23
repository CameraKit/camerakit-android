package com.camerakit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.camerakit.type.CameraFacing;
import com.camerakit.type.CameraFlash;
import com.camerakit.type.CameraSize;
import com.jpegkit.Jpeg;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * CameraKitView provides a high-level, easy to implement, and safe to use way to work with
 * the Android camera.
 *
 * @since v1.0.0
 */
public class CameraKitView extends GestureLayout {

    /**
     * Request code for a runtime permissions intent.
     */
    private static final int PERMISSION_REQUEST_CODE = 99107;

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
     * Flag for handling requesting the {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     * permission.
     */
    public static final int PERMISSION_LOCATION = 1 << 3;

    /**
     * Represents manifest runtime-permissions that may be used.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true,
            value = {PERMISSION_CAMERA, PERMISSION_MICROPHONE, PERMISSION_STORAGE, PERMISSION_LOCATION})
    @interface Permission {
    }

    /**
     *
     */
    public interface CameraListener {

        /**
         *
         */
        void onOpened();

        /**
         *
         */
        void onClosed();

    }

    /**
     *
     */
    public interface PreviewListener {

        /**
         *
         */
        void onStart();

        /**
         *
         */
        void onStop();

    }

    /**
     *
     */
    public interface ErrorListener {

        /**
         * @param view
         * @param error
         */
        void onError(CameraKitView view, CameraException error);

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
    public interface PermissionsListener {

        void onPermissionsSuccess();

        void onPermissionsFailure();

    }

    /**
     *
     */
    public interface ImageCallback {

        /**
         * @param view
         * @param jpeg
         */
        void onImage(CameraKitView view, byte[] jpeg);

    }

    /**
     *
     */
    public interface VideoCallback {

        /**
         * @param view
         * @param video
         */
        void onVideo(CameraKitView view, Object video);

    }

    public interface FrameCallback {

        /**
         * @param view
         * @param jpeg
         */
        void onFrame(CameraKitView view, byte[] jpeg);

    }

    private boolean mAdjustViewBounds;
    private float mAspectRatio;
    private int mFacing;
    private int mFlash;
    private int mFocus;
    private float mZoomFactor;
    private int mSensorPreset;
    private int mPreviewEffect;
    private int mPermissions;
    private float mImageMegaPixels;
    private int mImageJpegQuality;
    private GestureListener mGestureListener;
    private CameraListener mCameraListener;
    private PreviewListener mPreviewListener;
    private ErrorListener mErrorListener;
    private PermissionsListener mPermissionsListener;

    private static CameraFacing cameraFacing;
    private static CameraFlash cameraFlash;

    private CameraPreview mCameraPreview;

    public CameraKitView(Context context) {
        super(context);
        obtainAttributes(context, null);
    }

    public CameraKitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obtainAttributes(context, attrs);
    }

    public CameraKitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttributes(context, attrs);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraKitView);
        mAdjustViewBounds = a.getBoolean(R.styleable.CameraKitView_android_adjustViewBounds, false);
        mAspectRatio = a.getFloat(R.styleable.CameraKitView_camera_aspectRatio, -1f);

        mFacing = a.getInteger(R.styleable.CameraKitView_camera_facing, CameraKit.FACING_BACK);
        if (cameraFacing == CameraFacing.FRONT) {
            mFacing = CameraKit.FACING_FRONT;
        }

        mFlash = a.getInteger(R.styleable.CameraKitView_camera_flash, CameraKit.FLASH_OFF);
        if (cameraFlash == CameraFlash.ON) {
            mFlash = CameraKit.FLASH_ON;
        }

        mFocus = a.getInteger(R.styleable.CameraKitView_camera_focus, CameraKit.FOCUS_AUTO);
        mZoomFactor = a.getFloat(R.styleable.CameraKitView_camera_zoomFactor, 1.0f);
        mPermissions = a.getInteger(R.styleable.CameraKitView_camera_permissions, PERMISSION_CAMERA);
        mImageMegaPixels = a.getFloat(R.styleable.CameraKitView_camera_imageMegaPixels, 2f);
        mImageJpegQuality = a.getInteger(R.styleable.CameraKitView_camera_imageJpegQuality, 100);

        a.recycle();

        mCameraPreview = new CameraPreview(getContext());
        addView(mCameraPreview);

        mCameraPreview.setListener(new CameraPreview.Listener() {
            @Override
            public void onCameraOpened() {
                if (mCameraListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraListener.onOpened();
                        }
                    });
                }
            }

            @Override
            public void onCameraClosed() {
                if (mCameraListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraListener.onClosed();
                        }
                    });
                }
            }

            @Override
            public void onPreviewStarted() {
                if (mPreviewListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mPreviewListener.onStart();
                        }
                    });
                }
            }

            @Override
            public void onPreviewStopped() {
                if (mPreviewListener != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mPreviewListener.onStop();
                        }
                    });
                }
            }
        });
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
                } else if (mCameraPreview != null && mCameraPreview.getSurfaceSize().area() > 0) {
                    CameraSize previewSize = mCameraPreview.getSurfaceSize();

                    width = (int) (((float) height / (float) previewSize.getHeight()) * previewSize.getWidth());
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                }
            } else if (layoutParams.height == WRAP_CONTENT) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = 0;

                if (mAspectRatio > 0) {
                    height = (int) (width * mAspectRatio);
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else if (mCameraPreview != null && mCameraPreview.getSurfaceSize().area() > 0) {
                    CameraSize previewSize = mCameraPreview.getSurfaceSize();

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

    public void onStart() {
        if (isInEditMode()) {
            return;
        }

        List<String> missingPermissions = getMissingPermissions();
        if (Build.VERSION.SDK_INT >= 23 && missingPermissions.size() > 0) {
            Activity activity = null;
            Context context = getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }

            if (activity != null) {
                List<String> requestPermissions = new ArrayList<>();
                List<String> rationalePermissions = new ArrayList<>();
                for (String permission : missingPermissions) {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        requestPermissions.add(permission);
                    } else {
                        rationalePermissions.add(permission);
                    }
                }

                if (requestPermissions.size() > 0) {
                    activity.requestPermissions(requestPermissions.toArray(new String[requestPermissions.size()]), PERMISSION_REQUEST_CODE);
                }

                if (rationalePermissions.size() > 0 && mPermissionsListener != null) {
                    mPermissionsListener.onPermissionsFailure();
                }
            }

            return;
        }

        if (mPermissionsListener != null) {
            mPermissionsListener.onPermissionsSuccess();
        }

        setFlash(mFlash);
        setImageMegaPixels(mImageMegaPixels);

        cameraFacing = getFacing() == CameraKit.FACING_BACK ? CameraFacing.BACK : CameraFacing.FRONT;
        mCameraPreview.start(cameraFacing);
    }

    public void onStop() {
        if (isInEditMode()) {
            return;
        }

        mCameraPreview.stop();
    }

    public void onResume() {
        if (isInEditMode()) {
            return;
        }

        mCameraPreview.resume();
    }

    /**
     *
     */
    public void onPause() {
        if (isInEditMode()) {
            return;
        }

        mCameraPreview.pause();
    }

    /**
     * @param callback
     */
    public void captureImage(final ImageCallback callback) {
        mCameraPreview.capturePhoto(new CameraPreview.PhotoCallback() {
            @Override
            public void onCapture(@NotNull final byte[] jpeg) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onImage(CameraKitView.this, jpeg);
                    }
                });
            }
        });
    }

    /**
     *
     */
    public void startVideo() {

    }

    /**
     *
     */
    public void stopVideo() {

    }

    /**
     * @param callback
     */
    public void captureVideo(VideoCallback callback) {

    }

    /**
     *
     */
    public void captureFrame(FrameCallback callback) {

    }

    /**
     *
     */
    public void setFrameCallback(FrameCallback callback) {

    }

    /**
     * @return
     */
    private List<String> getMissingPermissions() {
        List<String> manifestPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT < 23) {
            return manifestPermissions;
        }

        if ((mPermissions | PERMISSION_CAMERA) == mPermissions) {
            String manifestPermission = Manifest.permission.CAMERA;
            if (getContext().checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED) {
                manifestPermissions.add(manifestPermission);
            }
        }

        if ((mPermissions | PERMISSION_MICROPHONE) == mPermissions) {
            String manifestPermission = Manifest.permission.RECORD_AUDIO;
            if (getContext().checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED) {
                manifestPermissions.add(manifestPermission);
            }
        }

        if ((mPermissions | PERMISSION_STORAGE) == mPermissions) {
            String manifestPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (getContext().checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED) {
                manifestPermissions.add(manifestPermission);
            }
        }

        if ((mPermissions | PERMISSION_LOCATION) == mPermissions) {
            String manifestPermission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (getContext().checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED) {
                manifestPermissions.add(manifestPermission);
            }
        }

        return manifestPermissions;
    }

    public void setPermissionsListener(PermissionsListener permissionsListener) {
        mPermissionsListener = permissionsListener;
    }

    /**
     */
    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> manifestPermissions = getMissingPermissions();

            if (manifestPermissions.size() > 0) {
                activity.requestPermissions(manifestPermissions.toArray(new String[manifestPermissions.size()]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int approvedPermissions = 0;
            int deniedPermissions = 0;

            for (int i = 0; i < permissions.length; i++) {
                int flag = 0;
                switch (permissions[i]) {
                    case Manifest.permission.CAMERA: {
                        flag = PERMISSION_CAMERA;
                        break;
                    }

                    case Manifest.permission.RECORD_AUDIO: {
                        flag = PERMISSION_MICROPHONE;
                        break;
                    }

                    case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                        flag = PERMISSION_STORAGE;
                        break;
                    }
                }

                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    approvedPermissions = approvedPermissions | flag;
                } else {
                    deniedPermissions = deniedPermissions | flag;
                }
            }

            onStart();
        }
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
     * @param facing one of {@link CameraKit.Facing}'s constants.
     * @see CameraKit#FACING_BACK
     * @see CameraKit#FACING_FRONT
     */
    public void setFacing(@CameraKit.Facing int facing) {
        mFacing = facing;
        switch (mCameraPreview.getLifecycleState()) {
            case PAUSED:
            case STARTED: {
                onStop();
                onStart();
                break;
            }

            case RESUMED: {
                onStop();
                onStart();
                onResume();
                break;
            }
        }
    }

    /**
     * @return one of {@link CameraKit.Facing}'s constants.
     * @see #setFacing(int)
     */
    @CameraKit.Facing
    public int getFacing() {
        return mFacing;
    }

    /**
     *
     */
    public void toggleFacing() {
        if (getFacing() == CameraKit.FACING_BACK) {
            setFacing(CameraKit.FACING_FRONT);
        } else {
            setFacing(CameraKit.FACING_BACK);
        }
    }

    /**
     * @param flash one of {@link CameraKit.Flash}'s constants.
     * @see CameraKit#FLASH_OFF
     * @see CameraKit#FLASH_ON
     * @see CameraKit#FLASH_AUTO
     * @see CameraKit#FLASH_TORCH
     */
    public void setFlash(@CameraKit.Flash int flash) {
        mFlash = flash;

        try {
            switch (flash) {
                case CameraKit.FLASH_OFF: {
                    cameraFlash = CameraFlash.OFF;
                    break;
                }
                case CameraKit.FLASH_ON: {
                    cameraFlash = CameraFlash.ON;
                    break;
                }
                case CameraKit.FLASH_AUTO: {
                    throw new CameraException("FLASH_AUTO is not supported in this version of CameraKit.");
                }
                case CameraKit.FLASH_TORCH: {
                    throw new CameraException("FLASH_TORCH is not supported in this version of CameraKit.");
                }
            }
        } catch(CameraException exception) {
            Log.e("CameraException: Flash", exception.getMessage());
            return;
        }

        mCameraPreview.setFlash(cameraFlash);
    }

    /**
     * Determine if device is capable of flash
     * @return boolean if device is capable of flash
     */
    public boolean hasFlash() {
        return mCameraPreview.hasFlash();
    }

    /**
     * Get supported flash types on device
     * @return array of supported flash types
     */
    public CameraFlash[] getSupportedFlashTypes() {
        return mCameraPreview.getSupportedFlashTypes();
    }

    /**
     * @return one of {@link CameraKit.Flash}'s constants.
     * @see #setFlash(int)
     */
    @CameraKit.Flash
    public int getFlash() {
        return mFlash;
    }

    /**
     * @param focus one of {@link CameraKit.Focus}'s constants.
     * @see CameraKit#FOCUS_OFF
     * @see CameraKit#FOCUS_AUTO
     * @see CameraKit#FOCUS_CONTINUOUS
     */
    public void setFocus(@CameraKit.Focus int focus) {
        mFocus = focus;
    }

    /**
     * @return one of {@link CameraKit.Focus}'s constants.
     * @see #setFocus(int)
     */
    @CameraKit.Focus
    public int getFocus() {
        return mFocus;
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
     * @param sensorPreset one of {@link CameraKit.SensorPreset}'s constants.
     * @see CameraKit#SENSOR_PRESET_NONE
     * @see CameraKit#SENSOR_PRESET_ACTION
     * @see CameraKit#SENSOR_PRESET_PORTRAIT
     * @see CameraKit#SENSOR_PRESET_LANDSCAPE
     * @see CameraKit#SENSOR_PRESET_NIGHT
     * @see CameraKit#SENSOR_PRESET_NIGHT_PORTRAIT
     * @see CameraKit#SENSOR_PRESET_THEATRE
     * @see CameraKit#SENSOR_PRESET_BEACH
     * @see CameraKit#SENSOR_PRESET_SNOW
     * @see CameraKit#SENSOR_PRESET_SUNSET
     * @see CameraKit#SENSOR_PRESET_STEADYPHOTO
     * @see CameraKit#SENSOR_PRESET_FIREWORKS
     * @see CameraKit#SENSOR_PRESET_SPORTS
     * @see CameraKit#SENSOR_PRESET_PARTY
     * @see CameraKit#SENSOR_PRESET_CANDLELIGHT
     * @see CameraKit#SENSOR_PRESET_BARCODE
     */
    public void setSensorPreset(@CameraKit.SensorPreset int sensorPreset) {
        mSensorPreset = sensorPreset;
    }

    /**
     * @return one of {@link CameraKit.SensorPreset}'s constants.
     * @see #setSensorPreset(int)
     */
    @CameraKit.SensorPreset
    public int getSensorPreset() {
        return mSensorPreset;
    }

    /**
     * @param previewEffect one of {@link CameraKit.PreviewEffect}'s constants.
     * @see CameraKit#PREVIEW_EFFECT_NONE
     * @see CameraKit#PREVIEW_EFFECT_MONO
     * @see CameraKit#PREVIEW_EFFECT_SOLARIZE
     * @see CameraKit#PREVIEW_EFFECT_SEPIA
     * @see CameraKit#PREVIEW_EFFECT_POSTERIZE
     * @see CameraKit#PREVIEW_EFFECT_WHITEBOARD
     * @see CameraKit#PREVIEW_EFFECT_BLACKBOARD
     * @see CameraKit#PREVIEW_EFFECT_AQUA
     */
    public void setPreviewEffect(@CameraKit.PreviewEffect int previewEffect) {
        mPreviewEffect = previewEffect;
    }

    /**
     * @return one of {@link CameraKit.PreviewEffect}'s constants.
     * @see #setPreviewEffect(int)
     */
    @CameraKit.PreviewEffect
    public int getPreviewEffect() {
        return mPreviewEffect;
    }

    /**
     * @param permissions
     */
    public void setPermissions(@Permission int permissions) {
        mPermissions = permissions;
    }

    /**
     * @return
     * @see #setPermissions(int)
     */
    @Permission
    public int getPermissions() {
        return mPermissions;
    }

    public void setImageMegaPixels(float imageMegaPixels) {
        mImageMegaPixels = imageMegaPixels;
        mCameraPreview.setImageMegaPixels(mImageMegaPixels);
    }

    public float getImageMegaPixels() {
        return mImageMegaPixels;
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

    /**
     * @param cameraListener
     */
    public void setCameraListener(CameraListener cameraListener) {
        mCameraListener = cameraListener;
    }

    /**
     * @return CameraListener
     */
    public CameraListener getCameraListener() {
        return mCameraListener;
    }

    /**
     * Delete CameraListener
     */
    public void removeCameraListener() {
        mCameraListener = null;
    }

    /**
     * @param previewListener
     */
    public void setPreviewListener(PreviewListener previewListener) {
        mPreviewListener = previewListener;
    }

    /**
     * @return PreviewListener
     */
    public PreviewListener getPreviewListener() {
        return mPreviewListener;
    }

    /**
     * Delete PreviewListener
     */
    public void removePreviewListener() {
        mPreviewListener = null;
    }

    /**
     * @param errorListener
     */
    public void setErrorListener(ErrorListener errorListener) {
        mErrorListener = errorListener;
    }

    /**
     * @return ErrorListener
     * @see #setErrorListener(ErrorListener)
     */
    public ErrorListener getErrorListener() {
        return mErrorListener;
    }

    /**
     * Delete ErrorListener
     */
    public void removeErrorListener() {
        mErrorListener = null;
    }

    public CameraSize getPreviewResolution() {
        if (mCameraPreview.getPreviewSize().area() == 0) {
            return null;
        }

        return mCameraPreview.getPreviewSize();
    }

    public CameraSize getPhotoResolution() {
        if (mCameraPreview.getPhotoSize().area() == 0) {
            return null;
        }

        return mCameraPreview.getPhotoSize();
    }

    /**
     *
     */
    public interface JpegCallback {
        void onJpeg(Jpeg jpeg);
    }

    /**
     *
     */
    public static class Size implements Comparable<Size> {

        private final int mWidth;
        private final int mHeight;

        public Size(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (this == o) {
                return true;
            } else if (o instanceof Size) {
                Size size = (Size) o;
                return mWidth == size.mWidth && mHeight == size.mHeight;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return mWidth + "x" + mHeight;
        }

        @Override
        public int hashCode() {
            return mHeight ^ ((mWidth << (Integer.SIZE / 2)) | (mWidth >>> (Integer.SIZE / 2)));
        }

        @Override
        public int compareTo(@NonNull Size another) {
            return mWidth * mHeight - another.mWidth * another.mHeight;
        }

    }

    /**
     *
     */
    public static class CameraException extends RuntimeException {

        public CameraException() {
            super();
        }

        public CameraException(String message) {
            super(message);
        }

        public CameraException(String message, Throwable cause) {
            super(message, cause);
        }

        public boolean isFatal() {
            return false;
        }

    }

}
