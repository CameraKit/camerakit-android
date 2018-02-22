package com.camerakit;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

abstract class SurfaceOutputView extends OutputView implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;

    public SurfaceOutputView(Context context, CameraSize previewResolution) {
        super(context, previewResolution);
    }

    @Override
    protected View createOutputView() {
        mTextureView = new TextureView(getContext());
        mTextureView.setSurfaceTextureListener(this);
        return mTextureView;
    }

    @Override
    public void stop() {
        super.stop();
        mTextureView.setSurfaceTextureListener(null);
    }

    @Override
    void attachSurface(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surface.setDefaultBufferSize(mPreviewResolution.getWidth(), mPreviewResolution.getHeight());
        attachSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (width == mPreviewResolution.getWidth() && height == mPreviewResolution.getHeight()) {
            attachSurface(surface);
        } else {
            surface.setDefaultBufferSize(mPreviewResolution.getWidth(), mPreviewResolution.getHeight());
            attachSurface(surface);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        detachSurface();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
