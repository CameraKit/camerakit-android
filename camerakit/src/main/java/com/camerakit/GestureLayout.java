package com.camerakit;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

public abstract class GestureLayout extends FrameLayout {

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    public GestureLayout(@NonNull Context context) {
        super(context);
        initialize();
    }

    public GestureLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GestureLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleGestureListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    protected abstract void onTap(float x, float y);

    protected abstract void onLongTap(float x, float y);

    protected abstract void onDoubleTap(float x, float y);

    protected abstract void onPinch(float ds, float dsx, float dsy);

    public void performTap(float x, float y) {
        onTap(x, y);
    }

    public void performLongTap(float x, float y) {
        onLongTap(x, y);
    }

    public void performDoubleTap(float x, float y) {
        onDoubleTap(x, y);
    }

    public void performPinch(float dsx, float dsy) {
        float ds = (float) Math.sqrt((dsx * dsx) + (dsy * dsy));
        onPinch(ds, dsx, dsy);
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            performTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            performDoubleTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongTap(e.getX() / (float) getWidth(), e.getY() / (float) getHeight());
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float dsx = detector.getCurrentSpanX() - detector.getPreviousSpanX();
            float dsy = detector.getCurrentSpanY() - detector.getPreviousSpanY();
            performPinch(dsx, dsy);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

}
