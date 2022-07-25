package com.wonderkiln.camerakit;

import android.graphics.Rect;

import java.nio.ByteBuffer;

public class JpegTransformer {

    private ByteBuffer mHandler;

    public JpegTransformer(byte[] jpeg) {
        mHandler = jniStoreJpeg(jpeg, jpeg.length);
    }

    public byte[] getJpeg() {
        if (mHandler == null) return null;
        return jniCommit(mHandler);
    }

    public int getWidth() {
        if (mHandler == null) return 0;
        return jniGetWidth(mHandler);
    }

    public int getHeight() {
        if (mHandler == null) return 0;
        return jniGetHeight(mHandler);
    }

    public void rotate(int degrees) {
        if (mHandler != null) {
            jniRotate(mHandler, degrees);
        }
    }

    public void flipHorizontal() {
        if (mHandler != null) {
            jniFlipHorizontal(mHandler);
        }
    }

    public void flipVertical() {
        if (mHandler != null) {
            jniFlipVertical(mHandler);
        }
    }

    public void crop(Rect crop) {
        if (mHandler != null) {
            jniCrop(mHandler, crop.left, crop.top, crop.width(), crop.height());
        }
    }

    static {
        System.loadLibrary("yuvOperator");
        System.loadLibrary("jpegTransformer");
    }

    private native ByteBuffer jniStoreJpeg(byte[] jpeg, int size);

    private native byte[] jniCommit(ByteBuffer handler);

    private native int jniGetWidth(ByteBuffer handler);

    private native int jniGetHeight(ByteBuffer handler);

    private native void jniRotate(ByteBuffer handler, int degrees);

    private native void jniFlipHorizontal(ByteBuffer handler);

    private native void jniFlipVertical(ByteBuffer handler);

    private native void jniCrop(ByteBuffer handler, int left, int top, int width, int height);

}
