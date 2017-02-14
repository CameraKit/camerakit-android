package com.flurgle.camerakit.demo;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;

public class MediaHolder {

    private static WeakReference<File> video;
    private static WeakReference<Bitmap> image;

    public static void setVideo(@Nullable File video) {
        MediaHolder.video = video != null ? new WeakReference<>(video) : null;
    }

    @Nullable
    public static File getVideo() {
        return video != null ? video.get() : null;
    }

    public static void setImage(@Nullable Bitmap image) {
        MediaHolder.image = image != null ? new WeakReference<>(image) : null;
    }

    @Nullable
    public static Bitmap getImage() {
        return image != null ? image.get() : null;
    }

    public static void dispose() {
        setVideo(null);
        setImage(null);
    }

}
