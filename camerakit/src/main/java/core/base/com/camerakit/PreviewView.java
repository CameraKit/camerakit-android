package com.camerakit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

public abstract class PreviewView extends FrameLayout {

    protected SurfaceHolder mSurfaceHolder;
    protected SurfaceCallback mSurfaceCallback;

    public PreviewView(Context context) {
        super(context);
    }

    public PreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    abstract void inflate();

    void setSurfaceCallback(SurfaceCallback surfaceCallback) {
        mSurfaceCallback = surfaceCallback;
        if (mSurfaceHolder == null) {
            surfaceCallback().detachSurface();
        } else {
            surfaceCallback().attachSurface(mSurfaceHolder);
        }
    }

    protected SurfaceCallback surfaceCallback() {
        return new SurfaceCallback() {
            @Override
            public void attachSurface(SurfaceHolder surfaceHolder) {
                mSurfaceHolder = surfaceHolder;
                if (mSurfaceCallback != null) {
                    mSurfaceCallback.attachSurface(surfaceHolder);
                }
            }

            @Override
            public void detachSurface() {
                mSurfaceHolder = null;
                if (mSurfaceCallback != null) {
                    mSurfaceCallback.detachSurface();
                }
            }
        };
    }

    interface SurfaceCallback {
        void attachSurface(SurfaceHolder surfaceHolder);
        void detachSurface();
    }

}
