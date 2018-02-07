package com.camerakit;

import java.io.File;

public class PhotoFile {

    private Photo mPhoto;
    private File mFile;

    public PhotoFile(Photo photo, File file) {
        mPhoto = photo;
        mFile = file;
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public File getFile() {
        return mFile;
    }

}
