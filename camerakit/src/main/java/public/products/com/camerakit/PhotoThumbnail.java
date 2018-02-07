package com.camerakit;

import java.io.File;

public class PhotoThumbnail {

    private Photo mPhoto;
    private File mFile;

    public PhotoThumbnail(Photo photo, File file) {
        mPhoto = photo;
        mFile = file;

//        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
//        exif.getThumbnail()
    }

}
