package com.camerakit;

public class PhotoBytes {

    private Photo mPhoto;
    private byte[] mJpeg;

    public PhotoBytes(Photo photo, byte[] jpeg) {
        mPhoto = photo;
        mJpeg = jpeg;
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public byte[] getBytes() {
        return mJpeg;
    }

}
