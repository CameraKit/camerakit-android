package com.camerakit;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

abstract class OutputView extends FrameLayout {

    private Display mDisplay;
    private RotationListener mRotationListener;

    protected CameraSize mPreviewResolution;
    private View mOutputView;

    private OutputView(Context context) {
        super(context);
    }

    private OutputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private OutputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OutputView(Context context, CameraSize previewResolution) {
        this(context);
        this.mPreviewResolution = previewResolution;

        mOutputView = createOutputView();
        mOutputView.setLayoutParams(new FrameLayout.LayoutParams(previewResolution.getWidth(), previewResolution.getHeight()));
        addView(mOutputView);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            mDisplay = windowManager.getDefaultDisplay();
            mRotationListener = new RotationListener(getContext(), mDisplay) {
                @Override
                void onRotation(int displayRotation) {
                    dispatchOrientation(displayRotation);
                }
            };
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float widthRatio = (float) (right - left) / (float) mPreviewResolution.getWidth();
        float heightRatio = (float) (bottom - top) / (float) mPreviewResolution.getHeight();

        if (widthRatio > heightRatio) {
            int width = (right - left);
            int height = (int) (mPreviewResolution.getHeight() * widthRatio);
            int heightOffset = (height - (bottom - top)) / 2;

            mOutputView.layout(0, -heightOffset, width, height - heightOffset);
        } else if (heightRatio > widthRatio) {
            int width = (int) (mPreviewResolution.getWidth() * heightRatio);
            int height = (bottom - top);
            int widthOffset = (width - (right - left)) / 2;

            mOutputView.layout(-widthOffset, 0, width - widthOffset, height);
        }
    }

    protected abstract View createOutputView();

    public void stop() {
        mRotationListener.disable();
    }

    private void dispatchOrientation(int displayOrientation) {
        switch (displayOrientation) {
            case Surface.ROTATION_0: {
                onOrientationChanged(0);
                break;
            }

            case Surface.ROTATION_90: {
                onOrientationChanged(90);
                break;
            }

            case Surface.ROTATION_180: {
                onOrientationChanged(180);
                break;
            }

            case Surface.ROTATION_270: {
                onOrientationChanged(270);
                break;
            }
        }
    }

    abstract void onOrientationChanged(int displayOrientation);

    abstract void attachSurface(SurfaceHolder surfaceHolder);

    abstract void attachSurface(SurfaceTexture surfaceTexture);

    abstract void detachSurface();

    private static abstract class RotationListener extends OrientationEventListener {

        private Display display;
        private int lastKnownDisplayRotation = -1;

        RotationListener(Context context, Display display) {
            super(context);
            this.display = display;
            enable();
            onRotation(this.display.getRotation());
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            final int displayRotation = display.getRotation();
            if (lastKnownDisplayRotation != displayRotation) {
                lastKnownDisplayRotation = displayRotation;
                onRotation(displayRotation);
            }
        }

        abstract void onRotation(int displayRotation);

    }

}
