package com.camerakit;

import android.content.Context;

public class PhotoJpeg extends Photo {

    private byte[] mJpeg;

    public PhotoJpeg(Context context, byte[] jpeg) {
        super(context);
        mJpeg = jpeg;
    }

    public byte[] getBytes() {
        return mJpeg;
    }

}
