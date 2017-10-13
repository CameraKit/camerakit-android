package com.wonderkiln.camerakit.demo;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.wonderkiln.camerakit.Size;

import java.io.File;

public class ResultHolder {

    private static Bitmap image;
    private static File video;
    private static Size nativeCaptureSize;
    private static long timeToCallback;


    public static void setImage(@Nullable Bitmap image) {
        ResultHolder.image = image;
    }

    @Nullable
    public static Bitmap getImage() {
        return image;
    }

    public static void setVideo(@Nullable File video) {
        ResultHolder.video = video;
    }

    @Nullable
    public static File getVideo() {
        return video;
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
