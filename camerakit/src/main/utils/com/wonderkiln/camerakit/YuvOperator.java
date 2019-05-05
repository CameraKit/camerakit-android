package com.wonderkiln.camerakit;

import java.nio.ByteBuffer;

public class YuvOperator {

    private ByteBuffer handler;

    private int width;
    private int height;

    private YuvOperator() {
    }

    public YuvOperator(byte[] yuv, int width, int height) {
        storeYuvData(yuv, width, height);
        this.width = width;
        this.height = height;
    }

    private void storeYuvData(byte[] yuv, int width, int height) {
        if (handler != null) freeYuvData();
        handler = jniStoreYuvData(yuv, width, height);
    }

    public void rotate(int rotation) {
        if (handler == null) return;
        if (rotation == 90) jniRotateYuvCw90(handler);
        else if (rotation == 180) jniRotateYuv180(handler);
        else if (rotation == 270) jniRotateYuvCcw90(handler);
    }

    public byte[] getYuvData() {
        byte[] yuv = jniGetYuvData(handler);
        freeYuvData();
        return yuv;
    }

    private void freeYuvData() {
        if (handler == null) return;
        jniFreeYuvData(handler);
        handler = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (handler == null) return;
        freeYuvData();
    }

    static {
        System.loadLibrary("yuvOperator");
    }

    private native ByteBuffer jniStoreYuvData(byte[] data, int width, int height);

    private native void jniRotateYuvCcw90(ByteBuffer handler);

    private native void jniRotateYuvCw90(ByteBuffer handler);

    private native void jniRotateYuv180(ByteBuffer handler);

    private native byte[] jniGetYuvData(ByteBuffer handler);

    private native void jniFreeYuvData(ByteBuffer handler);

}
