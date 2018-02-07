package com.camerakit;

import android.view.SurfaceHolder;
import android.view.View;


interface Preview {

    View getView();

    void setSurfaceCallback(SurfaceCallback surfaceCallback);
    void setGestureCallback(GestureCallback gestureCallback);

    interface SurfaceCallback {
        void attachSurface(SurfaceHolder surfaceHolder);
        void detachSurface();
    }

    interface GestureCallback {
        void onTap(float x, float y);
        void onLongTap(float x, float y);
        void onDoubleTap(float x, float y);
        void onPinch(float scaleFactor, boolean start);
    }

}
