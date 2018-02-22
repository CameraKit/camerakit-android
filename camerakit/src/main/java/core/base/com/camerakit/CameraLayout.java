package com.camerakit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.OrientationListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

abstract class CameraLayout extends FrameLayout {

    private static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private Display mDisplay;
    private OrientationEventListener mOrientationEventListener;

    private int mLastKnownDisplayOrientation = 0;
    private int mLastKnownDeviceOrientation = 0;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    public CameraLayout(@NonNull Context context) {
        super(context);
        initialize();
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mScaleGestureListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);

        mOrientationEventListener = new OrientationListener(getContext());
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


    void enableOrientationDetection() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            mDisplay = windowManager.getDefaultDisplay();
            mOrientationEventListener.enable();
            dispatchOnDisplayOrDeviceOrientationChanged(DISPLAY_ORIENTATIONS.get(mDisplay.getRotation()));        }
    }

    void disableOrientationDetection() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    void dispatchOnDisplayOrDeviceOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;

        // If we don't have accelerometers, we can't detect the device orientation.
        if (mOrientationEventListener.canDetectOrientation()) {
            onOrientationChanged(displayOrientation, mLastKnownDeviceOrientation);
        } else {
            onOrientationChanged(displayOrientation, displayOrientation);
        }
    }

    abstract void onOrientationChanged(int displayOrientation, int deviceOrientation);

    private class OrientationListener extends OrientationEventListener {

        private int mLastKnownDisplayRotation = -1;

        public OrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mDisplay == null) {
                return;
            }

            boolean displayOrDeviceOrientationChanged = false;

            final int displayRotation = mDisplay.getRotation();
            if (mLastKnownDisplayRotation != displayRotation) {
                mLastKnownDisplayRotation = displayRotation;
                displayOrDeviceOrientationChanged = true;
            }

            int deviceOrientation;
            if (orientation >= 60 && orientation <= 140) {
                // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                deviceOrientation = 270;
            } else if (orientation >= 140 && orientation <= 220) {
                deviceOrientation = 180;
            } else if (orientation >= 220 && orientation <= 300) {
                // the mDisplay.getRotation stuff is flipped for 90 & 270 vs. deviceOrientation here. This keeps it consistent.
                deviceOrientation = 90;
            } else {
                deviceOrientation = 0;
            }

            if (mLastKnownDeviceOrientation != deviceOrientation) {
                mLastKnownDeviceOrientation = deviceOrientation;
                displayOrDeviceOrientationChanged = true;
            }

            if (displayOrDeviceOrientationChanged) {
                dispatchOnDisplayOrDeviceOrientationChanged(DISPLAY_ORIENTATIONS.get(displayRotation));
            }
        }

    }

}
