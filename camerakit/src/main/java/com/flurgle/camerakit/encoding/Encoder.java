package com.flurgle.camerakit.encoding;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Encoder {

    protected BlockingQueue<Frame> queue = new ArrayBlockingQueue<Frame>(100);
    int width;
    int height;
    int cameraFacing;
    long startMS = 0;
    byte[] rotatedFrameData = null;
    byte[] planeManagedData = null;
    boolean encoderStarted = false;

    public Encoder(int cameraFacing, int width, int height) {
        this.cameraFacing = cameraFacing;
        this.height = height;
        this.width = width;
    }

    abstract public void encode(byte[] rawData);

    public abstract void stopEncoder();

    public boolean hasEncoderStarted() {
        return encoderStarted;
    }

    public void setStartMS(long ms) {
        this.startMS = ms;
    }

    public static void YV12toYUV420PackedSemiPlanar(final byte[] input, byte[] out, final int width, final int height) {

        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;

        for (int i = 0; i < input.length; i++) {
            if (i < frameSize)
                out[i] = input[i];
            if (i < (qFrameSize)) {
                out[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb (U)
                out[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
            }
        }
    }

    static byte[] NV21toYUV420p(byte[] data, int width, int height) {
        int len_target = (width * height * 3) / 2;
        byte[] buf_target = new byte[len_target];
        System.arraycopy(data, 0, buf_target, 0, width * height);

        for (int i = 0; i < (width * height / 4); i++) {
            buf_target[(width * height) + i] = data[(width * height) + 2 * i + 1];
            buf_target[(width * height) + (width * height / 4) + i] = data[(width * height) + 2 * i];
        }
        return buf_target;
    }

    public void rotateYUV420Degree90(byte[] data, byte[] output, int imageWidth, int imageHeight) {
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                output[i] = data[y * imageWidth + x];
                i++;
            }
        }

        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                output[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                output[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
    }

}