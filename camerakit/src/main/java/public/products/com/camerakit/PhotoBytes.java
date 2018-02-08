package com.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoBytes extends Photo {

    PhotoBytes(PhotoBytes photo) {
        super(photo.mContext);
        mBytes = photo.mBytes;
    }

    PhotoBytes(Photo photo, byte[] bytes) {
        super(photo);
        mBytes = bytes;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public CameraPending<PhotoBitmap> toBitmap() {
        CameraPending<PhotoBitmap> output = new CameraPending<>();

        output.run(pending -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length);
            pending.set(new PhotoBitmap(PhotoBytes.this, bitmap));
        });

        return output;
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

        output.run(pending -> {
            File directory = new File(mContext.getFilesDir(), folderName);

            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, fileName);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.write(getBytes());
            out.flush();
            out.close();

            pending.set(new PhotoFile(PhotoBytes.this, imageFile));
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

        output.run(pending -> {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + folderName);

            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            File imageFile = new File(directory, fileName);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.write(getBytes());
            out.flush();
            out.close();

            MediaScannerConnection.scanFile(mContext, new String[]{imageFile.getAbsolutePath()}, null, (path, uri) -> {
                pending.set(new PhotoFile(PhotoBytes.this, imageFile));
            });
        });

        return output;
    }

}
