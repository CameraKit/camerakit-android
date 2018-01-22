package com.wonderkiln.camerakit;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

abstract class PreviewImpl {

    interface Callback {
        void onSurfaceChanged(SurfaceHolder surfaceHolder);
    }

    private Callback mCallback;

    private int mWidth;
    private int mHeight;

    protected int mPreviewWidth;
    protected int mPreviewHeight;
    protected int mPreviewFormat;

    protected SurfaceHolder mSurfaceHolder;

    void setCallback(Callback callback) {
        if (callback == null) {
            mCallback = new Callback() {
                @Override
                public void onSurfaceChanged(SurfaceHolder surfaceHolder) {

                }
            };

            return;
        }

        mCallback = callback;

        if (mSurfaceHolder != null) {
            mCallback.onSurfaceChanged(mSurfaceHolder);
        }
    }

    abstract Surface getSurface();

    abstract View getView();

    abstract Class getOutputClass();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract boolean isReady();

    protected void dispatchSurfaceChanged(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mCallback.onSurfaceChanged(surfaceHolder);
    }

    SurfaceHolder getSurfaceHolder() {
        return null;
    }

    SurfaceTexture getSurfaceTexture() {
        return null;
    }

    void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    void setPreviewParameters(final int width, final int height, final int format) {
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
        this.mPreviewFormat = format;
    }

    int getPreviewWidth() {
        return mPreviewWidth;
    }

    int getPreviewHeight() {
        return mPreviewHeight;
    }

    int getPreviewFormat() {
        return mPreviewFormat;
    }

}
