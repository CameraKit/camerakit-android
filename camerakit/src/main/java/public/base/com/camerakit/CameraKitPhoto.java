package com.camerakit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class CameraKitPhoto {

    private Context mContext;
    private byte[] mBytes;

    public interface PhotoCallback<T> {
        void onPhoto(T photo);
    }

    private CameraKitPhoto() {
    }

    CameraKitPhoto(Context context, byte[] bytes) {
        mContext = context;
        mBytes = bytes;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public void getBitmap(PhotoCallback<Bitmap> callback) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length);
        callback.onPhoto(bitmap);
    }

    public void getFile(PhotoCallback<File> callback) {
        getFile("camerakit", callback);
    }

    public void getFile(String folder, PhotoCallback<File> callback) {
        getFile(folder, System.currentTimeMillis() + ".jpg", callback);
    }

    public void getFile(String folder, String file, PhotoCallback<File> callback) {
        try {
            File directory = new File(mContext.getFilesDir(), folder);

            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, file);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.write(getBytes());
            out.flush();
            out.close();

            callback.onPhoto(imageFile);
        } catch (Exception e) {
        }
    }

    public void getGalleryFile(PhotoCallback<File> callback) {
        getGalleryFile("camerakit", callback);
    }

    public void getGalleryFile(String folder, PhotoCallback<File> callback) {
        getGalleryFile(folder, System.currentTimeMillis() + ".jpg", callback);
    }

    public void getGalleryFile(String folder, String file, PhotoCallback<File> callback) {
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + folder);
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, file);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.write(getBytes());
            out.flush();
            out.close();

            MediaScannerConnection.scanFile(mContext, new String[]{imageFile.getAbsolutePath()}, null, (path, uri) -> {
                callback.onPhoto(imageFile);
            });
        } catch (Exception e) {
        }
    }

    public void getThumbnail(PhotoCallback<CameraKitPhoto> callback) {
        getFile((file -> {
            try {
                ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                if (exifInterface.hasThumbnail()) {
                    byte[] thumbnail = exifInterface.getThumbnail();
                    if (thumbnail != null) {
                        callback.onPhoto(new CameraKitPhoto(mContext, thumbnail));
                    }
                }
            } catch (Exception e) {
            }
        }));
    }

}
