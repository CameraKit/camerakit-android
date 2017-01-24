package com.flurgle.camerakit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
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
import android.widget.FrameLayout;

import com.flurgle.camerakit.utils.DisplayOrientationDetector;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.flurgle.camerakit.CameraKit.Constants.FACING_BACK;
import static com.flurgle.camerakit.CameraKit.Constants.FACING_FRONT;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_AUTO;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_OFF;
import static com.flurgle.camerakit.CameraKit.Constants.FLASH_ON;
import static com.flurgle.camerakit.CameraKit.Constants.PICTURE_MODE_QUALITY;
import static com.flurgle.camerakit.CameraKit.Constants.PICTURE_MODE_SPEED;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_INVISIBLE;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_OFF;
import static com.flurgle.camerakit.CameraKit.Constants.TAP_TO_FOCUS_VISIBLE;

public class CameraView extends FrameLayout {

    private static final int PERMISSION_REQUEST_CAMERA = 16;

    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    @interface Facing {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_AUTO})
    @interface Flash {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PICTURE_MODE_QUALITY, PICTURE_MODE_SPEED})
    @interface PictureMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAP_TO_FOCUS_VISIBLE, TAP_TO_FOCUS_INVISIBLE, TAP_TO_FOCUS_OFF})
    @interface TapToFocus {
    }

    private int mFacing;
    private int mDefaultFacing;

    private int mFlash;
    private int mDefaultFlash;

    private int mPictureMode;

    private boolean mCropOutput;

    private int mTapToFocus;
    private boolean mAutoFocus;

    private boolean mAdjustViewBounds;

    private boolean mWaitingForPermission;

    private CameraListener mCameraListener;
    private DisplayOrientationDetector mDisplayOrientationDetector;

    private CameraViewImpl mCameraImpl;

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

                if (attr == R.styleable.CameraView_ckPictureMode) {
                    mPictureMode = a.getInteger(R.styleable.CameraView_ckPictureMode, PICTURE_MODE_QUALITY);
                }

                if (attr == R.styleable.CameraView_ckCropOutput) {
                    mCropOutput = a.getBoolean(R.styleable.CameraView_ckCropOutput, false);
                }

                if (attr == R.styleable.CameraView_ckTapToFocus) {
                    mTapToFocus = a.getInteger(R.styleable.CameraView_ckTapToFocus, TAP_TO_FOCUS_VISIBLE);
                }

                if (attr == R.styleable.CameraView_ckAutoFocus) {
                    mAutoFocus = a.getBoolean(R.styleable.CameraView_ckAutoFocus, true);
                }

                if (attr == R.styleable.CameraView_android_adjustViewBounds) {
                    mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
                }
            }
            a.recycle();
        }

        final PreviewImpl preview = new TextureViewPreview(context, this);
        mCameraImpl = new Camera1(mCameraListener, preview);

        setFacing(mFacing);
        setFlash(mFlash);

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

    public void setPictureMode(@PictureMode int pictureMode) {
        this.mPictureMode = pictureMode;
    }

    public void setCropOutput(boolean cropOutput) {
        this.mCropOutput = cropOutput;
    }

    public void setTapToFocus(@TapToFocus int tapToFocus) {
        this.mTapToFocus = tapToFocus;
    }

    public void setAutoFocus(boolean autoFocus) {
        this.mAutoFocus = autoFocus;
    }

    public void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener = new CameraListenerMiddleWare(cameraListener);
        mCameraImpl.setCameraListener(mCameraListener);
    }

    public void capturePicture() {
        switch (mPictureMode) {
            case PICTURE_MODE_QUALITY:
                mCameraImpl.capturePicture();
                break;
            case PICTURE_MODE_SPEED:
                mCameraImpl.captureStill();
                break;
        }
    }

    public void startRecordingVideo() {
        mCameraImpl.startVideo();
    }

    public void stopRecordingVideo() {
        mCameraImpl.endVideo();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraListener.onVideoTaken(new File(getContext().getExternalFilesDir(null), "video.mp4"));
            }
        }, 1000);
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
            ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.CAMERA }, PERMISSION_REQUEST_CAMERA);
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

    protected class CameraListenerMiddleWare extends CameraListener {

        private CameraListener mCameraListener;

        public CameraListenerMiddleWare(CameraListener cameraListener) {
            this.mCameraListener = cameraListener;
        }

        @Override
        public void onCameraOpened() {
            super.onCameraOpened();
            mCameraListener.onCameraOpened();
        }

        @Override
        public void onCameraClosed() {
            super.onCameraClosed();
            mCameraListener.onCameraClosed();
        }

        @Override
        public void onPictureTaken(byte[] picture) {
            super.onPictureTaken(picture);
            mCameraListener.onPictureTaken(picture);
        }

        @Override
        public void onVideoTaken(File video) {
            super.onVideoTaken(video);
            mCameraListener.onVideoTaken(video);
        }

    }

}
