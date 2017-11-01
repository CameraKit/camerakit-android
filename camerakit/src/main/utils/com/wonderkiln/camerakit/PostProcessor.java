package com.wonderkiln.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.wonderkiln.camerakit.CameraKit.Constants.FACING_FRONT;

public class PostProcessor {

    private byte[] picture;
    private int jpegQuality;
    private int facing;
    private AspectRatio cropAspectRatio;

    public PostProcessor(byte[] picture) {
        this.picture = picture;
    }

    public void setJpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public void setCropOutput(AspectRatio aspectRatio) {
        this.cropAspectRatio = aspectRatio;
    }

    public byte[] getJpeg() {
        Bitmap bitmap;
        try {
            bitmap = getBitmap();
        } catch (IOException e) {
            return null;
        }

        BitmapOperation bitmapOperation = new BitmapOperation(bitmap);
        new ExifPostProcessor(picture).apply(bitmapOperation);

        if (facing == FACING_FRONT) {
            bitmapOperation.flipBitmapHorizontal();
        }

        bitmap = bitmapOperation.getBitmap();

        if (cropAspectRatio != null) {
            new CenterCrop(bitmap.getWidth(), bitmap.getHeight(), cropAspectRatio).apply(bitmapOperation);
        }

        bitmap = bitmapOperation.getBitmapAndFree();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
        return out.toByteArray();
    }

    private Bitmap getBitmap() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(picture, 0, picture.length, options);
        return BitmapRegionDecoder.newInstance(
                picture,
                0,
                picture.length,
                true
        ).decodeRegion(new Rect(0, 0, options.outWidth, options.outHeight), null);
    }

    private static class ExifPostProcessor {

        private int orientation = ExifInterface.ORIENTATION_UNDEFINED;

        public ExifPostProcessor(byte[] picture) {
            try {
                orientation = getExifOrientation(new ByteArrayInputStream(picture));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void apply(BitmapOperation bitmapOperation) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    bitmapOperation.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmapOperation.rotateBitmap(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    bitmapOperation.flipBitmapVertical();
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    bitmapOperation.rotateBitmap(90);
                    bitmapOperation.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmapOperation.rotateBitmap(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    bitmapOperation.rotateBitmap(270);
                    bitmapOperation.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmapOperation.rotateBitmap(270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                    break;
            }
        }

        private static int getExifOrientation(InputStream inputStream) throws IOException {
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }

    }

    private static class CenterCrop {

        private int width;
        private int height;
        private AspectRatio aspectRatio;

        public CenterCrop(int width, int height, AspectRatio aspectRatio) {
            this.width = width;
            this.height = height;
            this.aspectRatio = aspectRatio;
        }

        public void apply(BitmapOperation bitmapOperation) {
            Rect crop = getCrop(width, height, aspectRatio);
            bitmapOperation.cropBitmap(crop.left, crop.top, crop.right, crop.bottom);
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

    }

}
