package com.camerakit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceViewPreview extends PreviewView {

    private SurfaceView mSurfaceView;

    public SurfaceViewPreview(Context context) {
        super(context);
        inflate();
    }

    public SurfaceViewPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate();
    }

    public SurfaceViewPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate();
    }

    @Override
    void inflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.surface_view_preview, this);

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {
                surfaceCallback().attachSurface(holder);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCallback().attachSurface(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                surfaceCallback().attachSurface(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceCallback().detachSurface();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mSurfaceView != null) {
            mSurfaceView.layout(left, top, right, bottom);
        }
    }

}
