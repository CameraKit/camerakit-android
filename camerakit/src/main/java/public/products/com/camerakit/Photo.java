package com.camerakit;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class Photo extends CameraProduct {

    private byte[] mJpeg;
    private Throwable mError;

    private CameraPending<PhotoJpeg> mPendingJpeg;

    public Photo(Context context) {
        super(context);
    }

    void set(byte[] jpeg) {
        mJpeg = jpeg;
        if (mPendingJpeg != null) {
            mPendingJpeg.set(new PhotoJpeg(mContext, mJpeg));
        }
    }

    void set(Throwable error) {
        mError = error;
    }

    public CameraPending<PhotoJpeg> toJpegBytes() {
        mPendingJpeg = new CameraPending<>();

        if (mJpeg != null) {
            mPendingJpeg.set(new PhotoJpeg(mContext, mJpeg));
        }

        return mPendingJpeg;
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

        toJpegBytes().whenReady(jpeg -> {
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

               pending.set(new PhotoFile(mContext, imageFile));
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

        toJpegBytes().whenReady(jpeg -> {
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
                    pending.set(new PhotoFile(mContext, imageFile));
                });
            });
        });

        return output;
    }


}
