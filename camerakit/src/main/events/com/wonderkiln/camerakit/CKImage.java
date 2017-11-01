package com.wonderkiln.camerakit;

public class CKImage extends CKEvent {

    private byte[] jpeg;

    CKImage(byte[] jpeg) {
        super(TYPE_IMAGE_CAPTURED);
        this.jpeg = jpeg;
    }

    public byte[] getJpeg() {
        return jpeg;
    }

}
