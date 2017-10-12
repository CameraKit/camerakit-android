package com.wonderkiln.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExifUtil {

    public static int getExifOrientation(byte[] picture){
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            orientation = getExifOrientation(new ByteArrayInputStream(picture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    public static Bitmap decodeBitmapWithRotation(byte[] picture, boolean frontFacing) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
        Matrix matrix = getBitmapRotation(picture, frontFacing);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Matrix getBitmapRotation(byte[] picture, boolean frontFacing) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            orientation = getExifOrientation(new ByteArrayInputStream(picture));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
        }

        if(frontFacing){
            matrix.postScale(-1, 1);
        }

        return matrix;
    }

    private static int getExifOrientation(InputStream inputStream) throws IOException {
        ExifInterface exif = new ExifInterface(inputStream);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }
}
