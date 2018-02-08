package com.camerakit;

import android.graphics.Bitmap;

public class PhotoBitmap extends PhotoBytes {

    private Bitmap mBitmap;

    PhotoBitmap(PhotoBytes photo, Bitmap bitmap) {
        super(photo);
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
