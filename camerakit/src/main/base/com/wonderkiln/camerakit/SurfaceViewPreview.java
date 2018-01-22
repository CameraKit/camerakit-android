package com.wonderkiln.camerakit;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class SurfaceViewPreview extends PreviewImpl {

    private Context mContext;
    private ViewGroup mParent;
    private SurfaceViewContainer mContainer;
    private SurfaceView mSurfaceView;

    private int mDisplayOrientation;

    SurfaceViewPreview(final Context context, ViewGroup parent) {
        this.mContext = context;
        this.mParent = parent;

        final View view = View.inflate(context, R.layout.surface_view, parent);
        mContainer = view.findViewById(R.id.surface_view_container);
        mSurfaceView = mContainer.findViewById(R.id.surface_view);

        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                setSize(width, height);
                if (isReady()) dispatchSurfaceChanged(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                setSize(0, 0);
            }
        });
    }

    @Override
    SurfaceHolder getSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    @Override
    Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    @Override
    View getView() {
        return mContainer;
    }

    @Override
    Class getOutputClass() {
        return SurfaceHolder.class;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        mContainer.setDisplayOrientation(displayOrientation);
    }

    @Override
    boolean isReady() {
        return getWidth() != 0 && getHeight() != 0 && getWidth() == getPreviewWidth() && getHeight() == getPreviewHeight();
    }

    @TargetApi(15)
    @Override
    void setPreviewParameters(final int width, final int height, final int format) {
        super.setPreviewParameters(width, height, format);
        mContainer.setPreviewSize(new Size(width, height));
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                getSurfaceHolder().setFixedSize(getPreviewWidth(), getPreviewHeight());
            }
        });
    }

}
