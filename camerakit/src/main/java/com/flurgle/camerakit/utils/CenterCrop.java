package com.flurgle.camerakit.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class CenterCrop {

    private byte[] croppedJpeg;

    public CenterCrop(YuvImage yuv, AspectRatio targetRatio) {
        Rect crop = getCrop(yuv.getWidth(), yuv.getHeight(), targetRatio);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(crop, 100, out);
        this.croppedJpeg = out.toByteArray();
    }

    public CenterCrop(byte[] jpeg, AspectRatio targetRatio) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        Rect crop = getCrop(bitmap.getWidth(), bitmap.getHeight(), targetRatio);
        bitmap = Bitmap.createBitmap(bitmap, crop.left, crop.top, crop.width(), crop.height());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        this.croppedJpeg = out.toByteArray();
    }

    private static Rect getCrop(int currentWidth, int currentHeight, AspectRatio targetRatio) {
        AspectRatio currentRatio = AspectRatio.of(currentWidth, currentHeight);

        Rect crop;
        if (currentRatio.toFloat() > targetRatio.toFloat()) {
            int width = (int) (currentHeight * targetRatio.toFloat());
            int widthOffset = (currentWidth - width) / 2;
            crop = new Rect(widthOffset, 0, currentWidth - widthOffset, currentHeight);
        } else {
            int height = (int) (currentWidth * targetRatio.inverse().toFloat());
            int heightOffset = (currentHeight - height) / 2;
            crop = new Rect(0, heightOffset, currentWidth, currentHeight - heightOffset);
        }

        return crop;
    }

    public byte[] getJpeg() {
        return croppedJpeg;
    }

}
