package com.wonderkiln.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.media.ExifInterface;

import java.io.ByteArrayInputStream;
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

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        BitmapOperator bitmapOperator = new BitmapOperator(bitmap);
        bitmap.recycle();

        ExifPostProcessor exifPostProcessor = new ExifPostProcessor(picture);
        exifPostProcessor.apply(bitmapOperator);

        if (facing == FACING_FRONT) {
            bitmapOperator.flipBitmapHorizontal();
        }

        if (cropAspectRatio != null) {
            int cropWidth = width;
            int cropHeight = height;
            if (exifPostProcessor.areDimensionsFlipped()) {
                cropWidth = height;
                cropHeight = width;
            }

            new CenterCrop(cropWidth, cropHeight, cropAspectRatio).apply(bitmapOperator);
        }

        return bitmapOperator.getJpegAndFree(jpegQuality);
    }

    private Bitmap getBitmap() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(picture, 0, picture.length, options);

        BitmapFactory.Options regionOptions = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapRegionDecoder.newInstance(
                picture,
                0,
                picture.length,
                true
        ).decodeRegion(new Rect(0, 0, options.outWidth, options.outHeight), regionOptions);
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

        public void apply(BitmapOperator bitmapOperator) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    bitmapOperator.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmapOperator.rotateBitmap(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    bitmapOperator.flipBitmapVertical();
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    bitmapOperator.rotateBitmap(90);
                    bitmapOperator.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmapOperator.rotateBitmap(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    bitmapOperator.rotateBitmap(270);
                    bitmapOperator.flipBitmapHorizontal();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmapOperator.rotateBitmap(270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                    break;
            }
        }

        public boolean areDimensionsFlipped() {
            switch (orientation) {
                case ExifInterface.ORIENTATION_TRANSPOSE:
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_TRANSVERSE:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return true;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                case ExifInterface.ORIENTATION_ROTATE_180:
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                    return false;
            }

            return false;
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

        public void apply(BitmapOperator bitmapOperator) {
            Rect crop = getCrop(width, height, aspectRatio);
            bitmapOperator.cropBitmap(crop.left, crop.top, crop.right, crop.bottom);
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
