package com.wonderkiln.camerakit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CKImage extends CKEvent {

    private byte[] jpeg;

    CKImage(byte[] jpeg) {
        super(TYPE_IMAGE_CAPTURED);
        this.jpeg = jpeg;
    }

    public byte[] getJpeg() {
        return jpeg;
    }

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
    }

}
