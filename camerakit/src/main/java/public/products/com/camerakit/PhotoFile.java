package com.camerakit;

import android.content.Context;

import java.io.File;

public class PhotoFile extends Photo {

    private File mFile;

    public PhotoFile(Context context, File file) {
        super(context);
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

}
