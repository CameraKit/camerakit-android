package com.camerakit;


import android.hardware.Camera;

public class CameraSize implements Comparable<CameraSize> {

    private final int mWidth;
    private final int mHeight;

    public CameraSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public CameraSize inverse() {
        return new CameraSize(mHeight, mWidth);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof CameraSize) {
            CameraSize size = (CameraSize) o;
            return mWidth == size.mWidth && mHeight == size.mHeight;
        }
        return false;
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }

    @Override
    public int hashCode() {
        return mHeight ^ ((mWidth << (Integer.SIZE / 2)) | (mWidth >>> (Integer.SIZE / 2)));
    }

    @Override
    public int compareTo(CameraSize another) {
        return mWidth * mHeight - another.mWidth * another.mHeight;
    }

    public static CameraSize get(Camera.Size size) {
        return new CameraSize(size.width, size.height);
    }

}
