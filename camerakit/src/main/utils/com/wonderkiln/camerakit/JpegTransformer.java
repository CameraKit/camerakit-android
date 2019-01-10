package com.wonderkiln.camerakit;

import android.graphics.Rect;

import java.nio.ByteBuffer;

public class JpegTransformer {

    private ByteBuffer mHandler;

    public JpegTransformer(byte[] jpeg) {
        mHandler = jniStoreJpeg(jpeg, jpeg.length);
    }

    public byte[] getJpeg() {
        return jniCommit(mHandler);
    }

    public int getWidth() {
        return jniGetWidth(mHandler);
    }

    public int getHeight() {
        return jniGetHeight(mHandler);
    }

    public void rotate(int degrees) {
        jniRotate(mHandler, degrees);
    }

    public void flipHorizontal() {
        jniFlipHorizontal(mHandler);
    }

    public void flipVertical() {
        jniFlipVertical(mHandler);
    }

    public void crop(Rect crop) {
        jniCrop(mHandler, crop.left, crop.top, crop.width(), crop.height());
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
