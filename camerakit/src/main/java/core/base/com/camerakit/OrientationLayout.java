package com.camerakit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

abstract class OrientationLayout extends FrameLayout {

    private final OrientationEventListener mOrientationEventListener;

    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private Display mDisplay;

    private int mLastKnownDisplayOrientation = 0;
    private int mLastKnownDeviceOrientation = 0;

    public OrientationLayout(Context context) {
        super(context);
        mOrientationEventListener = new OrientationListener(context);
    }

    public OrientationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOrientationEventListener = new OrientationListener(context);
    }

    public OrientationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOrientationEventListener = new OrientationListener(context);
    }

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
