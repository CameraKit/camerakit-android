package com.camerakit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

class PreviewSurfaceView extends SurfaceView implements Preview, SurfaceHolder.Callback {

    private SurfaceCallback mSurfaceCallback;
    private GestureCallback mGestureCallback;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    private SurfaceHolder mSurfaceHolder;

    public PreviewSurfaceView(Context context) {
        super(context);

        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetector(context, mGestureListener);
    }

    private PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getHolder().addCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getHolder().removeCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        if (mSurfaceCallback != null) {
            mSurfaceCallback.attachSurface(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mSurfaceHolder = surfaceHolder;
        if (mSurfaceCallback != null) {
            mSurfaceCallback.attachSurface(surfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = null;
        if (mSurfaceCallback != null) {
            mSurfaceCallback.detachSurface();
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceCallback(SurfaceCallback surfaceCallback) {
        mSurfaceCallback = surfaceCallback;
        if (mSurfaceHolder != null && mSurfaceCallback != null) {
            mSurfaceCallback.attachSurface(mSurfaceHolder);
        }
    }

    @Override
    public void setGestureCallback(GestureCallback gestureCallback) {
        mGestureCallback = gestureCallback;
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGestureCallback != null) {
                mGestureCallback.onTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mGestureCallback != null) {
                mGestureCallback.onDoubleTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            }
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mGestureCallback != null) {
                mGestureCallback.onLongTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            }
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mGestureCallback != null) {
                mGestureCallback.onPinch(detector.getScaleFactor(), false);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (mGestureCallback != null) {
                mGestureCallback.onPinch(detector.getScaleFactor(), true);
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

}
