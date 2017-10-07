package com.wonderkiln.camerakit.demo;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.wonderkiln.camerakit.Size;

public class ResultHolder {

    private static Bitmap image;
    private static Size nativeCaptureSize;
    private static long timeToCallback;


    public static void setImage(@Nullable Bitmap image) {
        ResultHolder.image = image;
    }

    @Nullable
    public static Bitmap getImage() {
        return image;
    }

    public static void setNativeCaptureSize(@Nullable Size nativeCaptureSize) {
        ResultHolder.nativeCaptureSize = nativeCaptureSize;
    }

    @Nullable
    public static Size getNativeCaptureSize() {
        return nativeCaptureSize;
    }

    public static void setTimeToCallback(long timeToCallback) {
        ResultHolder.timeToCallback = timeToCallback;
    }

    public static long getTimeToCallback() {
        return timeToCallback;
    }

    public static void dispose() {
        setImage(null);
        setNativeCaptureSize(null);
        setTimeToCallback(0);
    }

}
