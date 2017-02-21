package com.flurgle.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CenterCrop {

    private byte[] croppedJpeg;

    public CenterCrop(YuvImage yuv, AspectRatio targetRatio, int jpegCompression) {
        Rect crop = getCrop(yuv.getWidth(), yuv.getHeight(), targetRatio);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(crop, jpegCompression, out);
        this.croppedJpeg = out.toByteArray();
    }

    public CenterCrop(byte[] jpeg, AspectRatio targetRatio, int jpegCompression) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);

        Rect crop = getCrop(options.outWidth, options.outHeight, targetRatio);
        try {
            Bitmap bitmap = BitmapRegionDecoder.newInstance(
                    jpeg,
                    0,
                    jpeg.length,
                    true
            ).decodeRegion(crop, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, jpegCompression, out);
            this.croppedJpeg = out.toByteArray();
        } catch (IOException e) {
            Log.e("CameraKit", e.toString());
        }
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
