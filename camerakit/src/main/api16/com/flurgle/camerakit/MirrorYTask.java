package com.flurgle.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class MirrorYTask implements Runnable {

    private byte[] data;
    private OnMirrorYDoneListener onMirrorYDoneListener;

    public MirrorYTask(byte[] data, OnMirrorYDoneListener onMirrorYDoneListener) {
        this.data = data;
        this.onMirrorYDoneListener = onMirrorYDoneListener;
    }

    @Override
    public void run() {
        Bitmap rawImage = BitmapFactory.decodeByteArray(data, 0, data.length);

        float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
        Matrix matrix = new Matrix();
        Matrix matrixMirrorY = new Matrix();
        matrixMirrorY.setValues(mirrorY);
        matrix.postConcat(matrixMirrorY);

        Bitmap correctedImage = Bitmap.createBitmap(rawImage, 0, 0, rawImage.getWidth(), rawImage.getHeight(), matrix, true);
        rawImage.recycle();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        correctedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] correctedData = stream.toByteArray();

        onMirrorYDoneListener.onMirrorYDone(correctedData);
    }

    interface OnMirrorYDoneListener {

        void onMirrorYDone(byte[] correctedData);
    }
}