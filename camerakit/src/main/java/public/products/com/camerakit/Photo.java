package com.camerakit;

import android.content.Context;

public class Photo extends CameraProduct {

    protected byte[] mBytes;
    protected Throwable mError;

    private CameraPending<PhotoBytes> mPendingBytes;

    Photo(Context context) {
        super(context);
    }

    Photo(Photo photo) {
        super(photo.mContext);

        if (photo.mBytes != null) {
            mBytes = photo.mBytes;
        }

        if (photo.mError != null) {
            mError = photo.mError;
        }
    }

    void set(byte[] jpeg) {
        mBytes = jpeg;
        if (mPendingBytes != null) {
            mPendingBytes.set(new PhotoBytes(Photo.this, mBytes));
        }
    }

    void set(Throwable error) {
        mError = error;
    }

    public CameraPending<PhotoBytes> toBytes() {
        mPendingBytes = new CameraPending<>();

        if (mBytes != null) {
            mPendingBytes.set(new PhotoBytes(Photo.this, mBytes));
        }

        return mPendingBytes;
    }


}
