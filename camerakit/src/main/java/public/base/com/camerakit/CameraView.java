package com.camerakit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CameraView extends OrientationLayout implements PreviewView.SurfaceCallback {

    private static final int CK_FACING_BACK = 0;
    private static final int CK_FACING_FRONT = 1;

    private boolean mAdjustViewBounds;
    private String mAspectRatio;

    private CameraFacing mCameraFacing;

    private CameraApi mCameraApi;
    private CameraExecutor mCameraExecutor;
    private PreviewView mPreviewView;

    private List<CameraModule> mModules;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CameraView, 0, 0);
        mCameraFacing = CameraFacing.get(array.getInteger(R.styleable.CameraView_camera_facing, CK_FACING_BACK));
        mAdjustViewBounds = array.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        mAspectRatio = array.getString(R.styleable.CameraView_camera_aspectRatio);

        mCameraExecutor = new CameraExecutor();
        mModules = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdjustViewBounds) {
            CameraAspectRatio cameraAspectRatio = null;
            if (mAspectRatio != null) {
                try {
                    String[] aspectsArray = mAspectRatio.split(":");
                    int widthAspect = Integer.parseInt(aspectsArray[0]);
                    int heightAspect = Integer.parseInt(aspectsArray[1]);
                    cameraAspectRatio = CameraAspectRatio.of(widthAspect, heightAspect);
                } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
                    throw new CameraException("XML attribute ck_aspectRatio on CameraView is setView but not valid.");
                }
            }

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams.width == WRAP_CONTENT && layoutParams.height == WRAP_CONTENT) {
                throw new CameraException("android:adjustViewBounds=true while both layout_width and layout_height are setView to wrap_content - only 1 is allowed.");
            } else if (layoutParams.width == WRAP_CONTENT) {
                int width = 0;
                int height = MeasureSpec.getSize(heightMeasureSpec);

                if (cameraAspectRatio != null) {
                    width = (int) (((float) height / (float) cameraAspectRatio.getY()) * cameraAspectRatio.getX());
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                } else if (getPreviewSize() != null) {
                    CameraSize previewSize = getPreviewSize();
                    if (getLastKnownDisplayOrientation() % 180 == 0) {
                        previewSize = previewSize.inverse();
                    }

                    width = (int) (((float) height / (float) previewSize.getHeight()) * previewSize.getWidth());
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                }
            } else if (layoutParams.height == WRAP_CONTENT) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = 0;

                if (cameraAspectRatio != null) {
                    height = (int) (((float) width / (float) cameraAspectRatio.getX()) * cameraAspectRatio.getY());
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else if (getPreviewSize() != null) {
                    CameraSize previewSize = getPreviewSize();
                    if (getLastKnownDisplayOrientation() % 180 == 0) {
                        previewSize = previewSize.inverse();
                    }

                    height = (int) (((float) width / (float) previewSize.getWidth()) * previewSize.getHeight());
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getPreviewSize() == null || mPreviewView == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        int width = right - left;
        int height = bottom - top;

        final View child = mPreviewView;

        CameraSize previewSize = getPreviewSize();
        if (getLastKnownDisplayOrientation() % 180 == 0) {
            previewSize = previewSize.inverse();
        }

        int previewWidth = previewSize.getWidth();
        int previewHeight = previewSize.getHeight();

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
        } else {
            final int scaledChildWidth = previewWidth * height / previewHeight;
            child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
        }
    }

    private CameraSize getPreviewSize() {
        if (mCameraApi == null) return null;
        if (mCameraApi.previewAttributes() == null) return null;
        if (mCameraApi.previewAttributes().supportedSizes() == null) return null;

        return mCameraApi.previewAttributes().supportedSizes().get(0);
    }

    @Override
    void onOrientationChanged(int displayOrientation, int deviceOrientation) {
        if (mCameraApi != null) {
            mCameraApi.previewApi()
                    .setDisplayOrientation(displayOrientation);
        }
    }

    @Override
    public void attachSurface(final SurfaceHolder surfaceHolder) {
        if (mCameraApi != null) {
            mCameraApi.previewApi()
                    .stop()
                    .then(() -> {
                        CameraSize previewSize = getPreviewSize();
                        mCameraApi.previewApi().setSize(previewSize.getWidth(), previewSize.getHeight());
                        invalidate();
                    })
                    .then(() -> mCameraApi.previewApi().setSurface(surfaceHolder))
                    .then(() -> mCameraApi.previewApi().start());
        }
    }

    @Override
    public void detachSurface() {
        if (mCameraApi != null) {
            mCameraApi.previewApi()
                    .stop();
        }
    }

    public void start() {
        if (mCameraApi != null) {
            return;
        }

        if (mPreviewView != null && indexOfChild(mPreviewView) >= 0) {
            removeView(mPreviewView);
            mPreviewView = null;
        }

        mCameraExecutor.start();
        mCameraApi = new Camera1(getContext(), mCameraExecutor, mCameraFacing);

        mCameraApi.connect()
                .result(previewView -> {
                    mPreviewView = previewView;
                    for (CameraModule module : mModules) {
                        module.setApi(mCameraApi);
                    }

                    post(() -> {
                        addView(mPreviewView);
                        mPreviewView.setSurfaceCallback(this);
                        enableOrientationDetection();
                    });
                })
                .error(error -> {
                    throw new CameraException("Failed to connect to Camera API.", error);
                });
    }

    public void stop() {
        disableOrientationDetection();

        if (mCameraApi != null) {
            mCameraApi.disconnect();
            mCameraApi = null;
        }

        if (mPreviewView != null) {
            mPreviewView.setSurfaceCallback(null);
        }

        mCameraExecutor.stop();

        for (CameraModule module : mModules) {
            module.setApi(null);
        }
    }

    public CameraFacing getFacing() {
        return mCameraFacing;
    }

    public CameraFacing setFacing(CameraFacing cameraFacing) {
        if (mCameraFacing == cameraFacing) {
            return mCameraFacing;
        }

        mCameraFacing = cameraFacing;

        stop();
        start();

        return mCameraFacing;
    }

    public CameraFacing faceFront() {
        return setFacing(CameraFacing.FRONT);
    }

    public CameraFacing faceBack() {
        return setFacing(CameraFacing.BACK);
    }

    public CameraFacing toggleFacing() {
        if (getFacing() == CameraFacing.BACK) {
            return faceFront();
        } else {
            return faceBack();
        }
    }

    public void use(CameraModule cameraModule) {
        cameraModule.setView(this);
        cameraModule.setApi(mCameraApi);
    }

}
