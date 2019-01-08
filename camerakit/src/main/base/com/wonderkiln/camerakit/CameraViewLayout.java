package com.wonderkiln.camerakit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

public abstract class CameraViewLayout extends FrameLayout {

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    public CameraViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public CameraViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new GestureDetector(context, onGestureListener);
        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    protected abstract CameraImpl getCameraImpl();

    protected abstract PreviewImpl getPreviewImpl();

    protected abstract void onZoom(float zoom, boolean start);

    protected abstract void onTapToFocus(float x, float y);

    protected abstract void onToggleFacing();

    private GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onToggleFacing();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onTapToFocus(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            return super.onSingleTapConfirmed(e);
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            onZoom(scaleGestureDetector.getScaleFactor(), false);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            onZoom(scaleGestureDetector.getScaleFactor(), true);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }

    };

}
