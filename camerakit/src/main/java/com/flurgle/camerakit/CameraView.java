package com.flurgle.camerakit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.flurgle.camerakit.CameraKit.Constants.FACING_BACK;
import static com.flurgle.camerakit.CameraKit.Constants.FACING_FRONT;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_AUTO;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_OFF;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_ON;
import static com.flurgle.camerakit.CameraKit.Constants.CAPTURE_MODE_STANDARD;
import static com.flurgle.camerakit.CameraKit.Constants.CAPTURE_MODE_STILL;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_VISIBLE;

public class CameraView extends FrameLayout {

    private static final int PERMISSION_REQUEST_CAMERA = 16;
    private static final int DEFAULT_CAPTURE_SIZE = -1;
    private static final int DEFAULT_JPEG_COMPRESSION = 100;

    @Facing
    private int mFacing;

    @Flash
    private int mFlash;

    @TapToFocus
    private int mTapToFocus;

    @CaptureMode
    private int mCaptureMode;

    private boolean mCropOutput;

    private boolean mAutoFocus;

    private float mCaptureSize;

    private int mJpegCompression;

    private boolean mAdjustViewBounds;

    private boolean mWaitingForPermission;

    private CameraListenerMiddleWare mCameraListener;
    private DisplayOrientationDetector mDisplayOrientationDetector;

    private CameraImpl mCameraImpl;
    private PreviewImpl mPreviewImpl;

    public CameraView(@NonNull Context context) {
        super(context, null);
    }

    @SuppressWarnings("all")
    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CameraView);
            final int n = a.getIndexCount();
            for (int i = 0; i < n; ++i) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.CameraView_ckFacing) {
                    mFacing = a.getInteger(R.styleable.CameraView_ckFacing, FACING_BACK);
                }

                if (attr == R.styleable.CameraView_ckFlash) {
                    mFlash = a.getInteger(R.styleable.CameraView_ckFlash, FLASH_OFF);
                }

                if (attr == R.styleable.CameraView_ckTapToFocus) {
                    mTapToFocus = a.getInteger(R.styleable.CameraView_ckTapToFocus, TAP_TO_FOCUS_VISIBLE);
                }

                if (attr == R.styleable.CameraView_ckCaptureMode) {
                    mCaptureMode = a.getInteger(R.styleable.CameraView_ckCaptureMode, CAPTURE_MODE_STANDARD);
                }

                if (attr == R.styleable.CameraView_ckCropOutput) {
                    mCropOutput = a.getBoolean(R.styleable.CameraView_ckCropOutput, false);
                }

                if (attr == R.styleable.CameraView_ckAutoFocus) {
                    mAutoFocus = a.getBoolean(R.styleable.CameraView_ckAutoFocus, true);
                }

                if (attr == R.styleable.CameraView_ckCaptureSize) {
                    mCaptureSize = a.getFloat(R.styleable.CameraView_ckCaptureSize, DEFAULT_CAPTURE_SIZE);
                }

                if (attr == R.styleable.CameraView_ckJpegCompression) {
                    mJpegCompression = a.getInteger(R.styleable.CameraView_ckJpegCompression, DEFAULT_JPEG_COMPRESSION);
                }

                if (attr == R.styleable.CameraView_android_adjustViewBounds) {
                    mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
                }
            }
            a.recycle();
        }

        mCameraListener = new CameraListenerMiddleWare();

        mPreviewImpl = new TextureViewPreview(context, this);
        mCameraImpl = new Camera1(mCameraListener, mPreviewImpl);

        setFacing(mFacing);
        setFlash(mFlash);
        setPictureMode(mCaptureMode);
        setCropOutput(mCropOutput);
        setTapToFocus(mTapToFocus);
        setAutoFocus(mAutoFocus);
        setCaptureSize(mCaptureSize);

        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mCameraImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDisplayOrientationDetector.enable(
                ViewCompat.isAttachedToWindow(this)
                        ? DisplayManagerCompat.getInstance(getContext()).getDisplay(Display.DEFAULT_DISPLAY)
                        : null
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        mDisplayOrientationDetector.disable();
        super.onDetachedFromWindow();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.facing = mFacing;
        state.flash = mFlash;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setFacing(ss.facing);
        setFlash(ss.flash);
    }

    public void start() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mWaitingForPermission = false;
            mCameraImpl.start();
        } else {
            requestCameraPermission();
        }
    }

    public void stop() {
        mCameraImpl.stop();
    }

    public void setFacing(@Facing int facing) {
        this.mFacing = facing;
        mCameraImpl.setFacing(facing);
    }

    @Facing
    public int toggleFacing() {
        switch (mFacing) {
            case FACING_BACK:
                setFacing(FACING_FRONT);
                break;

            case FACING_FRONT:
                setFacing(FACING_BACK);
                break;
        }

        return mFacing;
    }

    public void setFlash(@Flash int flash) {
        this.mFlash = flash;
        mCameraImpl.setFlash(flash);
    }

    @Flash
    public int toggleFlash() {
        switch (mFlash) {
            case FLASH_OFF:
                setFlash(FLASH_ON);
                break;

            case FLASH_ON:
                setFlash(FLASH_AUTO);
                break;

            case FLASH_AUTO:
                setFlash(FLASH_OFF);
                break;
        }

        return mFlash;
    }

    public void setPictureMode(@CaptureMode int pictureMode) {
        this.mCaptureMode = pictureMode;
    }

    @CaptureMode
    public int getPictureMode() {
        return mCaptureMode;
    }

    public void setCropOutput(boolean cropOutput) {
        this.mCropOutput = cropOutput;
    }

    public void setTapToFocus(@TapToFocus int tapToFocus) {
        this.mTapToFocus = tapToFocus;
        if (tapToFocus == CameraKit.Constants.TAP_TO_FOCUS_OFF) {
            mPreviewImpl.getView().setOnTouchListener(null);
        } else {
            mPreviewImpl.getView().setOnTouchListener(mTapToFocusOnTouchListener);
        }
    }

    public void setAutoFocus(boolean autoFocus) {
        this.mAutoFocus = autoFocus;
    }

    public void setCaptureSize(float captureSize) {
        this.mCaptureSize = captureSize;
    }

    public void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener.setCameraListener(cameraListener);
    }

    public void capturePicture() {
        switch (mCaptureMode) {
            case CAPTURE_MODE_STANDARD:
                mCameraImpl.captureStandard();
                break;
            case CAPTURE_MODE_STILL:
                mCameraImpl.captureStill();
                break;
        }
    }

    public void startRecordingVideo() {
        mCameraImpl.startVideo();
    }

    public void stopRecordingVideo() {
        mCameraImpl.endVideo();
    }

    public Size getPreviewSize() {
        return mCameraImpl != null ? mCameraImpl.getPreviewResolution() : null;
    }

    public Size getCaptureSize() {
        return mCameraImpl != null ? mCameraImpl.getCaptureResolution() : null;
    }

    private void requestCameraPermission() {
        Activity activity = null;
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                activity = (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (activity != null) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CAMERA);
            mWaitingForPermission = true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mWaitingForPermission) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                start();
            }
        }
    }

    private OnTouchListener mTapToFocusOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mCameraImpl.focus();
            }

            return false;
        }

    };


    protected static class SavedState extends BaseSavedState {

        @Facing
        private int facing;

        @Flash
        private int flash;

        @SuppressWarnings("WrongConstant")
        public SavedState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            flash = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeInt(flash);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        });

    }

    private class CameraListenerMiddleWare extends CameraListener {

        private CameraListener mCameraListener;

        @Override
        public void onCameraOpened() {
            super.onCameraOpened();
            getCameraListener().onCameraOpened();
        }

        @Override
        public void onCameraClosed() {
            super.onCameraClosed();
            getCameraListener().onCameraClosed();
        }

        @Override
        public void onPictureTaken(byte[] jpeg) {
            super.onPictureTaken(jpeg);
            if (mCropOutput) {
                int width = mCaptureMode == CAPTURE_MODE_STANDARD ? mCameraImpl.getCaptureResolution().getWidth() : mCameraImpl.getPreviewResolution().getWidth();
                int height = mCaptureMode == CAPTURE_MODE_STANDARD ? mCameraImpl.getCaptureResolution().getHeight() : mCameraImpl.getPreviewResolution().getHeight();
                AspectRatio outputRatio = AspectRatio.of(getWidth(), getHeight());
                getCameraListener().onPictureTaken(new CenterCrop(jpeg, outputRatio, mJpegCompression).getJpeg());
            } else {
                getCameraListener().onPictureTaken(jpeg);
            }
        }

        @Override
        public void onPictureTaken(YuvImage yuv) {
            super.onPictureTaken(yuv);
            if (mCropOutput) {
                AspectRatio outputRatio = AspectRatio.of(getWidth(), getHeight());
                getCameraListener().onPictureTaken(new CenterCrop(yuv, outputRatio, mJpegCompression).getJpeg());
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), mJpegCompression, out);
                getCameraListener().onPictureTaken(out.toByteArray());
            }
        }

        @Override
        public void onVideoTaken(File video) {
            super.onVideoTaken(video);
            getCameraListener().onVideoTaken(video);
        }

        public void setCameraListener(@Nullable CameraListener cameraListener) {
            this.mCameraListener = cameraListener;
        }

        @NonNull
        public CameraListener getCameraListener() {
            return mCameraListener != null ? mCameraListener : new CameraListener() {
            };
        }

    }

}
