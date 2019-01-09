package com.wonderkiln.camerakit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SurfaceViewContainer extends FrameLayout {

    private Size mPreviewSize;
    private int mDisplayOrientation;

    public SurfaceViewContainer(@NonNull Context context) {
        super(context);
    }

    public SurfaceViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            layoutChild(r - l, b - t);
        }
    }

    private void layoutChild(int width, int height) {
        final View child = getChildAt(0);

        int previewWidth = width;
        int previewHeight = height;
        if (mPreviewSize != null) {
            previewWidth = mPreviewSize.getWidth();
            previewHeight = mPreviewSize.getHeight();
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
        } else {
            final int scaledChildWidth = previewWidth * height / previewHeight;
            child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
        }
    }

    public void setPreviewSize(Size previewSize) {
        setPreviewSize(previewSize, mDisplayOrientation);
    }

    public void setPreviewSize(Size previewSize, int displayOrientation) {
        if (mDisplayOrientation == 0 || mDisplayOrientation == 180) {
            this.mPreviewSize = previewSize;
        } else if ((displayOrientation == 90 || displayOrientation == 270) && (mDisplayOrientation != 90 && mDisplayOrientation != 270)) {
            this.mPreviewSize = new Size(previewSize.getHeight(), previewSize.getWidth());
        }

        if (getChildCount() > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    layoutChild(getWidth(), getHeight());
                }
            });
        }
    }

    public void setDisplayOrientation(int displayOrientation) {
        if (mPreviewSize != null) {
            setPreviewSize(mPreviewSize, displayOrientation);
        } else {
            this.mDisplayOrientation = displayOrientation;
        }
    }

}
