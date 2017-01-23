package com.flurgle.camerakit;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class YuvUtils {

    public static byte[] createRGB(Image image) {
        ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
        byte[] y = new byte[bufferY.remaining()];
        bufferY.get(y);

        ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
        byte[] u = new byte[bufferU.remaining()];
        bufferU.get(u);

        ByteBuffer bufferV = image.getPlanes()[2].getBuffer();
        byte[] v = new byte[bufferV.remaining()];
        bufferV.get(v);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(y);
            outputStream.write(v);
            outputStream.write(u);
        } catch (IOException e) {

        }
        byte data[] = outputStream.toByteArray();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 50, out);

        return out.toByteArray();
    }

}

