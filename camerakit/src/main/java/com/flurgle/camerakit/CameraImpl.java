package com.flurgle.camerakit;

import android.view.View;

import com.flurgle.camerakit.types.Facing;
import com.flurgle.camerakit.types.Flash;
import com.flurgle.camerakit.utils.Size;

public abstract class CameraImpl {

    protected CameraListener mCameraListener;
    protected final PreviewImpl mPreview;

    CameraImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    View getView() {
        return mPreview.getView();
    }

    abstract void start();

    abstract void stop();

    abstract boolean isCameraOpened();

    abstract void setFacing(@Facing int facing);

    abstract void setFlash(@Flash int flash);

    abstract void setAutoFocus(boolean autoFocus);

    abstract boolean getAutoFocus();

    abstract void capturePicture();

    abstract void captureStill();

    abstract void startVideo();

    abstract void endVideo();

    abstract void focus();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract Size getCaptureSize();

    abstract Size getPreviewSize();

}
