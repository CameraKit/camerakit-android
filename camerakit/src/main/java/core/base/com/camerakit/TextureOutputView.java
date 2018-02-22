package com.camerakit;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

abstract class TextureOutputView extends OutputView implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;

    public TextureOutputView(Context context, CameraSize previewResolution) {
        super(context, previewResolution);
    }

    @Override
    protected View createOutputView() {
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.getHolder().addCallback(this);
        return mSurfaceView;
    }

    @Override
    public void stop() {
        super.stop();
        mSurfaceView.getHolder().removeCallback(this);
    }

    @Override
    void attachSurface(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        attachSurface(holder);

        holder.setFixedSize(mPreviewResolution.getWidth(), mPreviewResolution.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (width == mPreviewResolution.getWidth() && height == mPreviewResolution.getHeight()) {
            attachSurface(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        detachSurface();
    }

}
