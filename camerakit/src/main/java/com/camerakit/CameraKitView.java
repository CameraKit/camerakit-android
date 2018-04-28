package com.camerakit;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.jpegkit.Jpeg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;
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
    @interface Permission {}

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
        mFlash = a.getInteger(R.styleable.CameraKitView_camera_flash, CameraKit.FLASH_OFF);
        mFocus = a.getInteger(R.styleable.CameraKitView_camera_focus, CameraKit.FOCUS_AUTO);
        mZoomFactor = a.getFloat(R.styleable.CameraKitView_camera_zoomFactor, 1.0f);
        mPermissions = a.getInteger(R.styleable.CameraKitView_camera_permissions, PERMISSION_CAMERA);
        mImageMegaPixels = a.getFloat(R.styleable.CameraKitView_camera_imageMegaPixels, 2f);
        mImageJpegQuality = a.getInteger(R.styleable.CameraKitView_camera_imageJpegQuality, 100);

        a.recycle();
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
                } else if (mCameraPreview != null && mCameraPreview.getPreviewSize() != null) {
                    Size previewSize = mCameraPreview.getAdjustedPreviewSize();

                    width = (int) (((float) height / (float) previewSize.getHeight()) * previewSize.getWidth());
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                }
            } else if (layoutParams.height == WRAP_CONTENT) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = 0;

                if (mAspectRatio > 0) {
                    height = (int) (width * mAspectRatio);
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else if (mCameraPreview != null && mCameraPreview.getPreviewSize() != null) {
                    Size previewSize = mCameraPreview.getAdjustedPreviewSize();

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

    public void onResume() {
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

        removeAllViews();

        if (Build.VERSION.SDK_INT < 21) {
            mCameraPreview = new Camera1(getContext(), mFacing);
        } else {
            mCameraPreview = new Camera2(getContext(), mFacing);
        }

        addView(mCameraPreview);
    }

    /**
     *
     */
    public void onPause() {
        if (isInEditMode()) {
            return;
        }

        if (mCameraPreview != null) {
            mCameraPreview.stop();
            mCameraPreview = null;
        }
    }

    /**
     * @param callback
     */
    public void captureImage(final ImageCallback callback) {
        if (mCameraPreview != null) {
            mCameraPreview.captureImage(new JpegCallback() {
                @Override
                public void onJpeg(Jpeg jpeg) {
                    byte[] jpegBytes = jpeg.getJpegBytes();
                    jpeg.release();

                    callback.onImage(CameraKitView.this, jpegBytes);
                }
            });
        }
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
     *
     */
    public void setFacing(@CameraKit.Facing int facing) {
        mFacing = facing;

        if (mCameraPreview != null) {
            onPause();
            onResume();
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

        if (mCameraPreview != null) {
            mCameraPreview.reconfigure();
        }
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

    }

    /**
     * @return
     * @see #setPermissions(int)
     */
    @Permission
    public int getPermissions() {
        return mPermissions;
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
     *
     * @param cameraListener
     */
    public void setCameraListener(CameraListener cameraListener) {
        mCameraListener = cameraListener;
    }

    /**
     *
     * @return
     */
    public CameraListener getCameraListener() {
        return mCameraListener;
    }

    /**
     *
     * @param previewListener
     */
    public void setPreviewListener(PreviewListener previewListener) {
        mPreviewListener = previewListener;
    }

    /**
     *
     * @return
     */
    public PreviewListener getPreviewListener() {
        return mPreviewListener;
    }

    /**
     * @param errorListener
     */
    public void setErrorListener(ErrorListener errorListener) {
        mErrorListener = errorListener;
    }

    /**
     * @return
     * @see #setErrorListener(ErrorListener)
     */
    public ErrorListener getErrorListener() {
        return mErrorListener;
    }

    /**
     *
     */
    public static class Attributes {

        public int facing;

        public int sensorOrientation;

        public List<Integer> supportedFocuses;

        public List<CameraKitView.Size> supportedPreviewSizes;

        public List<CameraKitView.Size> supportedImageSizes;

    }

    /**
     *
     */
    interface CameraApi {

        Attributes getAttributes();

        void openCamera();

        void closeCamera();

        void startPreview();

        void stopPreview();

        void setDisplayRotation(int displayRotation);

        void setFlash(int flash);

        void setFocus(int focus);

        void captureImage(ImageCallback callback);

        interface ImageCallback {
            void onImage(byte[] data);
        }

    }

    /**
     *
     */
    abstract class CameraPreview extends ViewGroup {

        static final int EVENT_CAMERA_OPENED = 0;
        static final int EVENT_CAMERA_CLOSED = 1;
        static final int EVENT_CAMERA_ERROR = 2;

        static final int EVENT_SURFACE_CREATED = 3;
        static final int EVENT_SURFACE_CHANGED = 4;
        static final int EVENT_SURFACE_DESTROYED = 5;
        static final int EVENT_SURFACE_ERROR = 6;

        static final int EVENT_PREVIEW_STARTED = 7;
        static final int EVENT_PREVIEW_STOPPED = 8;
        static final int EVENT_PREVIEW_ERROR = 9;

        protected final CameraApi mApi;
        protected int mFacing;

        private Handler mHandler;
        private HandlerThread mHandlerThread;

        private int mDisplayRotation;
        private OrientationEventListener mOrientationEventListener;

        private Attributes mAttributes;
        private Size mPreviewSize;

        public CameraPreview(Context context) {
            this(context, CameraKit.FACING_BACK);
        }

        public CameraPreview(Context context, @CameraKit.Facing int facing) {
            super(context);

            mFacing = facing;
            mApi = getApi();

            mHandlerThread = new HandlerThread("CameraPreview@" + System.currentTimeMillis());
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());

            mOrientationEventListener = new OrientationEventListener(context) {
                @Override
                public void onOrientationChanged(int orientation) {
                    switch (orientation) {
                        case Surface.ROTATION_0: {
                            mDisplayRotation = 0;
                            break;
                        }

                        case Surface.ROTATION_90: {
                            mDisplayRotation = 90;
                            break;
                        }

                        case Surface.ROTATION_180: {
                            mDisplayRotation = 180;
                            break;
                        }

                        case Surface.ROTATION_270: {
                            mDisplayRotation = 270;
                            break;
                        }
                    }

                    mApi.setDisplayRotation(mDisplayRotation);
                }
            };


            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                mOrientationEventListener.onOrientationChanged(windowManager.getDefaultDisplay().getRotation());
            }
        }

        public void stop() {
            mApi.stopPreview();
            mApi.closeCamera();
        }

        public void reconfigure() {
        }

        @Nullable
        public Size getPreviewSize() {
            if (mPreviewSize == null && mAttributes != null) {
                int width = getWidth();
                int height = getHeight();

                if (mDisplayRotation % 180 != mAttributes.sensorOrientation % 180) {
                    width = getHeight();
                    height = getWidth();
                }

                Size bestSize = null;
                float bestRatio = Float.MAX_VALUE;

                for (Size size : mAttributes.supportedPreviewSizes) {
                    if (bestSize == null) {
                        float widthRatio = (float) width / (float) size.getWidth();
                        float heightRatio = (float) height / (float) size.getHeight();

                        bestSize = size;
                        bestRatio = Math.max(widthRatio, heightRatio);
                        continue;
                    }

                    float widthRatio = (float) width / (float) size.getWidth();
                    float heightRatio = (float) height / (float) size.getHeight();

                    float ratio = Math.max(widthRatio, heightRatio);
                    if (ratio < bestRatio) {
                        bestSize = size;
                        bestRatio = ratio;
                    }
                }

                mPreviewSize = bestSize;
            }

            return mPreviewSize;
        }

        public Size getAdjustedPreviewSize() {
            Size previewSize = getPreviewSize();
            if (previewSize == null) {
                return null;
            }

            if (mAttributes != null && mDisplayRotation % 180 != mAttributes.sensorOrientation % 180) {
                return new Size(previewSize.getHeight(), previewSize.getWidth());
            }

            return previewSize;
        }

        public void captureImage(final JpegCallback callback) {
            mApi.setFlash(mFlash);

            mApi.captureImage(new CameraApi.ImageCallback() {
                @Override
                public void onImage(byte[] data) {
                    Jpeg jpeg = new Jpeg(data);

                    int captureRotation = 0;
                    if (mFacing == CameraKit.FACING_FRONT) {
                        captureRotation = (mAttributes.sensorOrientation + mDisplayRotation) % 360;
                    } else {
                        captureRotation = (mAttributes.sensorOrientation - mDisplayRotation + 360) % 360;
                    }

                    if (mCameraPreview instanceof Camera1) {
                        jpeg.rotate(captureRotation);

                        if (mFacing == CameraKit.FACING_FRONT) {
                            jpeg.flipHorizontal();
                        }
                    }


                    callback.onJpeg(jpeg);
                }
            });
        }

        protected synchronized void dispatchEvent(final int event) {
            post(new Runnable() {
                @Override
                public void run() {
                    switch (event) {
                        case EVENT_CAMERA_OPENED: {
                            mAttributes = mApi.getAttributes();

                            if (mCameraListener != null) {
                                mCameraListener.onOpened();
                            }

                            mApi.startPreview();
                            break;
                        }

                        case EVENT_CAMERA_CLOSED: {
                            if (mCameraListener != null) {
                                mCameraListener.onClosed();
                            }

                            break;
                        }

                        case EVENT_CAMERA_ERROR: {
                            mApi.closeCamera();
                            break;
                        }

                        case EVENT_SURFACE_CREATED: {
                            mApi.openCamera();
                            break;
                        }

                        case EVENT_SURFACE_CHANGED: {
                            if (mAttributes != null) {
                                mApi.stopPreview();
                                mApi.startPreview();
                            }
                            break;
                        }

                        case EVENT_SURFACE_DESTROYED: {
                            mApi.stopPreview();
                            mApi.closeCamera();
                            break;
                        }

                        case EVENT_SURFACE_ERROR: {
                            break;
                        }

                        case EVENT_PREVIEW_STARTED: {
                            mApi.setDisplayRotation(mDisplayRotation);
                            requestLayout();

                            if (mPreviewListener != null) {
                                mPreviewListener.onStart();
                            }
                            break;
                        }

                        case EVENT_PREVIEW_STOPPED: {
                            if (mPreviewListener != null) {
                                mPreviewListener.onStop();
                            }
                            break;
                        }

                        case EVENT_PREVIEW_ERROR: {
                            break;
                        }
                    }
                }
            });

        }

        protected void background(Runnable runnable) {
            mHandler.post(runnable);
        }

        @Override
        protected abstract void onLayout(boolean changed, int left, int top, int right, int bottom);

        public abstract CameraApi getApi();

    }

    /**
     *
     */
    private class Camera1 extends CameraPreview implements SurfaceHolder.Callback {

        private final SurfaceView mSurfaceView;
        private final SurfaceHolder mSurfaceHolder;

        private Camera mCamera;
        private Camera.CameraInfo mCameraInfo;

        public Camera1(Context context) {
            this(context, CameraKit.FACING_BACK);
        }

        protected Camera1(Context context, @CameraKit.Facing int facing) {
            super(context, facing);

            mSurfaceView = new SurfaceView(context);

            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // CameraPreview:


        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            addView(mSurfaceView);
        }

        // ViewGroup:

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            Size previewSize = getAdjustedPreviewSize();
            if (previewSize != null) {
                float widthRatio = (float) (right - left) / (float) previewSize.getWidth();
                float heightRatio = (float) (bottom - top) / (float) previewSize.getHeight();

                if (widthRatio > heightRatio) {
                    int width = (right - left);
                    int height = (int) (previewSize.getHeight() * widthRatio);
                    int heightOffset = (height - (bottom - top)) / 2;

                    mSurfaceView.layout(0, -heightOffset, width, height - heightOffset);
                } else if (heightRatio > widthRatio) {
                    int width = (int) (previewSize.getWidth() * heightRatio);
                    int height = (bottom - top);
                    int widthOffset = (width - (right - left)) / 2;

                    mSurfaceView.layout(-widthOffset, 0, width - widthOffset, height);
                } else {
                    mSurfaceView.layout(left, top, right, bottom);
                }
            } else {
                mSurfaceView.layout(left, top, right, bottom);
            }
        }

        // SurfaceHolder.Callback:

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            dispatchEvent(EVENT_SURFACE_CREATED);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (holder.getSurface() == null) {
                dispatchEvent(EVENT_SURFACE_ERROR);
                return;
            }

            dispatchEvent(EVENT_SURFACE_CHANGED);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            dispatchEvent(EVENT_SURFACE_DESTROYED);
        }

        // CameraApi:

        @Override
        public CameraApi getApi() {
            return new CameraApi() {
                @Override
                public Attributes getAttributes() {
                    Attributes attributes = new Attributes();

                    attributes.facing = mFacing;
                    attributes.sensorOrientation = mCameraInfo.orientation;

                    attributes.supportedPreviewSizes = new ArrayList<>();
                    for (Camera.Size size : mCamera.getParameters().getSupportedPreviewSizes()) {
                        attributes.supportedPreviewSizes.add(new Size(size.width, size.height));
                    }

                    attributes.supportedImageSizes = new ArrayList<>();
                    for (Camera.Size size : mCamera.getParameters().getSupportedPictureSizes()) {
                        attributes.supportedImageSizes.add(new Size(size.width, size.height));
                    }

                    return attributes;
                }

                @Override
                public void openCamera() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            int cameraId = mFacing == CameraKit.FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
                            mCamera = Camera.open(cameraId);
                            mCameraInfo = new Camera.CameraInfo();
                            Camera.getCameraInfo(cameraId, mCameraInfo);

                            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                                @Override
                                public void onError(int error, Camera camera) {
                                    dispatchEvent(EVENT_CAMERA_ERROR);
                                }
                            });

                            dispatchEvent(EVENT_CAMERA_OPENED);
                        }
                    });
                }

                @Override
                public void closeCamera() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mCamera != null) {
                                mCamera.release();
                                mCamera = null;
                            }

                            dispatchEvent(EVENT_CAMERA_CLOSED);
                        }
                    });
                }

                @Override
                public void startPreview() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mSurfaceHolder.getSurface() != null) {
                                Size previewSize = getPreviewSize();
                                if (previewSize != null) {
                                    Camera.Parameters parameters = mCamera.getParameters();
                                    parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());

                                    if (parameters.getSupportedFlashModes().contains(FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
                                    }

                                    mCamera.setParameters(parameters);
                                }

                                try {
                                    mCamera.setPreviewDisplay(mSurfaceHolder);
                                } catch (IOException e) {
                                    dispatchEvent(EVENT_PREVIEW_ERROR);
                                    return;
                                }

                                mCamera.startPreview();

                                dispatchEvent(EVENT_PREVIEW_STARTED);
                            } else {
                                dispatchEvent(EVENT_SURFACE_ERROR);
                            }
                        }
                    });
                }

                @Override
                public void stopPreview() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mCamera != null) {
                                mCamera.stopPreview();
                            }

                            dispatchEvent(EVENT_PREVIEW_STOPPED);
                        }
                    });
                }


                @Override
                public void setDisplayRotation(final int displayRotation) {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mCamera != null) {
                                final int previewRotation;
                                if (mFacing == CameraKit.FACING_FRONT) {
                                    previewRotation = (360 - ((mCameraInfo.orientation + displayRotation) % 360)) % 360;
                                } else {
                                    previewRotation = (mCameraInfo.orientation - displayRotation + 360) % 360;
                                }
                                mCamera.setDisplayOrientation(previewRotation);
                            }
                        }
                    });
                }

                @Override
                public void setFlash(final int flash) {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            Camera.Parameters parameters = mCamera.getParameters();

                            switch (flash) {
                                case CameraKit.FLASH_OFF: {
                                    parameters.setFlashMode(FLASH_MODE_OFF);
                                    break;
                                }

                                case CameraKit.FLASH_ON: {
                                    parameters.setFlashMode(FLASH_MODE_ON);
                                    break;
                                }

                                case CameraKit.FLASH_AUTO: {
                                    parameters.setFlashMode(FLASH_MODE_AUTO);
                                    break;
                                }

                                case CameraKit.FLASH_TORCH: {
                                    parameters.setFlashMode(FLASH_MODE_TORCH);
                                    break;
                                }
                            }

                            mCamera.setParameters(parameters);
                        }
                    });
                }

                @Override
                public void setFocus(final int focus) {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            Camera.Parameters parameters = mCamera.getParameters();

                            switch (focus) {
                                case CameraKit.FOCUS_OFF: {
                                    break;
                                }

                                case CameraKit.FOCUS_AUTO: {
                                    break;
                                }

                                case CameraKit.FOCUS_CONTINUOUS: {
                                    if (parameters.getSupportedFlashModes().contains(FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
                                    }

                                    break;
                                }
                            }

                            mCamera.setParameters(parameters);
                        }
                    });
                }

                @Override
                public void captureImage(final ImageCallback callback) {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    startPreview();
                                    callback.onImage(data);
                                }
                            });
                        }
                    });
                }
            };
        }

    }

    /**
     *
     */
    @TargetApi(21)
    private class Camera2 extends CameraPreview implements TextureView.SurfaceTextureListener {

        private TextureView mTextureView;

        private CameraManager mCameraManager;
        private CameraDevice mCameraDevice;

        private CameraCharacteristics mCameraCharacteristics;

        private CameraCaptureSession mCaptureSession;

        private CaptureRequest.Builder mPreviewRequestBuilder;

        public Camera2(Context context) {
            this(context, CameraKit.FACING_BACK);
        }

        protected Camera2(Context context, @CameraKit.Facing int facing) {
            super(context, facing);

            mTextureView = new TextureView(context);
            mTextureView.setSurfaceTextureListener(this);
        }

        // CameraPreview:


        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            addView(mTextureView);
        }

        // ViewGroup:

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mTextureView.layout(left, top, right, bottom);

            Size previewSize = getAdjustedPreviewSize();
            if (previewSize == null) {
                return;
            }

            float viewSizeRatio = (float) right / (float) bottom;
            float previewSizeRatio = (float) previewSize.getWidth() / (float) previewSize.getHeight();

            float sx;
            float sy;

            if (viewSizeRatio < previewSizeRatio) {
                sx = previewSizeRatio / viewSizeRatio;
                sy = 1;
            } else {
                sx = 1;
                sy = viewSizeRatio / previewSizeRatio;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(sx, sy);

            float scaledWidth = right * sx;
            float scaledHeight = bottom * sy;
            float dx = (right - scaledWidth) / 2;
            float dy = (bottom - scaledHeight) / 2;

            matrix.postTranslate(dx, dy);
            mTextureView.setTransform(matrix);
        }

        // TextureView.SurfaceTextureListener:

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            dispatchEvent(EVENT_SURFACE_CREATED);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            dispatchEvent(EVENT_SURFACE_CHANGED);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            dispatchEvent(EVENT_SURFACE_DESTROYED);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        // CameraApi:

        @Override
        public CameraApi getApi() {
            return new CameraApi() {

                String mCameraId = null;
                boolean mFlashing = false;

                @Override
                public Attributes getAttributes() {
                    if (mCameraCharacteristics == null) {
                        return null;
                    }

                    StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map == null) {
                        return null;
                    }

                    Attributes attributes = new Attributes();

                    Integer sensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    if (sensorOrientation != null) {
                        attributes.sensorOrientation = sensorOrientation;
                    }

                    attributes.facing = mFacing;

                    attributes.supportedPreviewSizes = new ArrayList<>();
                    for (android.util.Size size : map.getOutputSizes(SurfaceTexture.class)) {
                        attributes.supportedPreviewSizes.add(new Size(size.getWidth(), size.getHeight()));
                    }

                    return attributes;
                }

                @Override
                @SuppressWarnings("MissingPermission")
                public void openCamera() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            int facingTarget = mFacing == CameraKit.FACING_BACK ? LENS_FACING_BACK : LENS_FACING_FRONT;
                            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

                            if (mCameraManager == null) {

                                return;
                            }

                            String[] cameraIdList;
                            try {
                                cameraIdList = mCameraManager.getCameraIdList();
                            } catch (Exception e) {

                                return;
                            }

                            for (final String cameraId : cameraIdList) {
                                try {
                                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                                    if (facing != null && facing != facingTarget) {
                                        continue;
                                    }

                                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                                    if (map == null) {
                                        continue;
                                    }

                                    mCameraCharacteristics = characteristics;

                                    mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {

                                        @Override
                                        public void onOpened(final @NonNull CameraDevice camera) {
                                            background(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCameraId = cameraId;
                                                    mCameraDevice = camera;
                                                    dispatchEvent(EVENT_CAMERA_OPENED);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onDisconnected(final @NonNull CameraDevice camera) {
                                            background(new Runnable() {
                                                @Override
                                                public void run() {
                                                    camera.close();
                                                    mCameraDevice = null;
                                                    mCameraId = null;

                                                    dispatchEvent(EVENT_CAMERA_CLOSED);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(final @NonNull CameraDevice camera, int error) {
                                            background(new Runnable() {
                                                @Override
                                                public void run() {
                                                    camera.close();
                                                    mCameraDevice = null;
                                                }
                                            });
                                        }

                                    }, null);
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                        }
                    });
                }

                @Override
                public void closeCamera() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mCameraDevice != null) {
                                mCameraDevice.close();
                                mCameraDevice = null;
                            }

                            dispatchEvent(EVENT_CAMERA_CLOSED);
                        }
                    });
                }

                @Override
                public void startPreview() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            Size previewSize = getPreviewSize();

                            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

                            Surface surface = new Surface(surfaceTexture);

                            try {
                                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                mPreviewRequestBuilder.addTarget(surface);

                                mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(final @NonNull CameraCaptureSession session) {
                                        background(new Runnable() {
                                            @Override
                                            public void run() {
                                                mCaptureSession = session;
                                                CaptureRequest previewRequest = mPreviewRequestBuilder.build();
                                                try {
                                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                                    session.setRepeatingRequest(previewRequest, null, null);

                                                    dispatchEvent(EVENT_PREVIEW_STARTED);
                                                } catch (Exception e) {
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onConfigureFailed(final @NonNull CameraCaptureSession session) {
                                        background(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        });
                                    }
                                }, null);
                            } catch (Exception e) {

                                return;
                            }
                        }
                    });
                }

                @Override
                public void stopPreview() {
                    background(new Runnable() {
                        @Override
                        public void run() {
                            if (mCaptureSession != null) {
                                mCaptureSession.close();
                                mCaptureSession = null;

                                dispatchEvent(EVENT_PREVIEW_STOPPED);
                            }
                        }
                    });
                }

                @Override
                public void setDisplayRotation(int displayRotation) {

                }

                @Override
                public void setFlash(int flash) {

                }

                @Override
                public void setFocus(int focus) {
                    background(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }

                @Override
                public void captureImage(final ImageCallback callback) {
                    Log.v("WonderKiln", "flashing: " + mFlashing + "  flash: " + mFlash);
                    if (mFlash == CameraKit.FLASH_ON && !mFlashing) {
                        try {
                            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                            mFlashing = true;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    captureImage(callback);
                                }
                            }, 1000);
                            return;
                        } catch (Exception e) {
                            Log.v("WonderKiln, ", e.toString());
                        }
                    }

                    background(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = mTextureView.getBitmap();

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            callback.onImage(byteArray);

                            if (mFlashing) {
                                try {
                                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                                    mFlashing = false;
                                } catch (Exception e) {
                                }
                            }
                        }
                    });
                }

            };
        }

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
