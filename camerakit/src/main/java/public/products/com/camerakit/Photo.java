package com.camerakit;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class Photo extends CameraProduct {

    private byte[] mJpeg;
    private Throwable mError;

    private CameraPending<PhotoBytes> mPendingBytes;

    public Photo(Context context) {
        super(context);
    }

    void set(byte[] jpeg) {
        mJpeg = jpeg;
        if (mPendingBytes != null) {
            mPendingBytes.set(new PhotoBytes(Photo.this, mJpeg));
        }
    }

    void set(Throwable error) {
        mError = error;
    }

    public CameraPending<PhotoBytes> toBytes() {
        mPendingBytes = new CameraPending<>();

        if (mJpeg != null) {
            mPendingBytes.set(new PhotoBytes(Photo.this, mJpeg));
        }

        return mPendingBytes;
    }

    public CameraPending<PhotoFile> toFile() {
        String label = "camerakit";
        return toGalleryFile(label);
    }

    public CameraPending<PhotoFile> toFile(String folderName) {
        return toGalleryFile(folderName, System.currentTimeMillis() + ".jpg");
    }

    public CameraPending<PhotoFile> toFile(String folderName, String fileName) {
        CameraPending<PhotoFile> output = new CameraPending<>();

        toBytes().whenReady(jpeg -> {
           output.run(pending -> {
               File directory = new File(mContext.getFilesDir(), folderName);

               if (!directory.isDirectory()) {
                   directory.mkdirs();
               }

               File imageFile = new File(directory, fileName);
               FileOutputStream out = new FileOutputStream(imageFile);
               out.write(jpeg.getBytes());
               out.flush();
               out.close();

               pending.set(new PhotoFile(Photo.this, imageFile));
           });
        });

        return output;
    }

    public CameraPending<PhotoFile> toGalleryFile() {
        String label = mContext.getApplicationInfo().loadLabel(mContext.getPackageManager()).toString();
        return toGalleryFile(label);
    }

    public CameraPending<PhotoFile> toGalleryFile(String folderName) {
        return toGalleryFile(folderName, System.currentTimeMillis() + ".jpg");
    }

    public CameraPending<PhotoFile> toGalleryFile(String folderName, String fileName) {
        CameraPending<PhotoFile> output = new CameraPending<>();

        toBytes().whenReady(jpeg -> {
            output.run(pending -> {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + folderName);

                if (!directory.isDirectory()) {
                    directory.mkdirs();
                }

                File imageFile = new File(directory, fileName);
                FileOutputStream out = new FileOutputStream(imageFile);
                out.write(jpeg.getBytes());
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(mContext, new String[]{imageFile.getAbsolutePath()}, null, (path, uri) -> {
                    pending.set(new PhotoFile(Photo.this, imageFile));
                });
            });
        });

        return output;
    }

    public CameraPending<PhotoThumbnail> toThumbnail() {
        CameraPending<PhotoThumbnail> output = new CameraPending<>();

        toFile().whenReady(file -> {

        });

        return output;
    }

    public CameraPending<PhotoThumbnail> toThumbnail(PhotoFile file) {
        CameraPending<PhotoThumbnail> output = new CameraPending<>();


        return output;
    }

}
