package com.camerakit;

import android.media.ExifInterface;

import java.io.File;

public class PhotoFile extends Photo {

    private File mFile;

    PhotoFile(PhotoFile photo) {
        super(photo);
        mFile = photo.mFile;
    }

    PhotoFile(Photo photo, File file) {
        super(photo);
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    public CameraPending<PhotoBytes> toThumbnail() {
        CameraPending<PhotoBytes> output = new CameraPending<>();

        output.run(pending -> {
            ExifInterface exifInterface = new ExifInterface(mFile.getAbsolutePath());
            if (exifInterface.hasThumbnail()) {
                byte[] thumbnail = exifInterface.getThumbnail();
                if (thumbnail != null) {
                    pending.set(new PhotoBytes(this, thumbnail));
                    return;
                }
            }

            pending.set(new CameraException("No thumbnail available from " + mFile.getAbsolutePath()));
        });

        return output;
    }

}
