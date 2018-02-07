package com.camerakit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;


public class CameraAspectRatio implements Comparable<CameraAspectRatio>, Parcelable {

    private final static SparseArray<SparseArray<CameraAspectRatio>> sCache = new SparseArray<>(16);

    private final int mX;
    private final int mY;

    private CameraAspectRatio(int x, int y) {
        mX = x;
        mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public boolean matches(CameraSize size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        int x = size.getWidth() / gcd;
        int y = size.getHeight() / gcd;
        return mX == x && mY == y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof CameraAspectRatio) {
            CameraAspectRatio ratio = (CameraAspectRatio) o;
            return mX == ratio.mX && mY == ratio.mY;
        }
        return false;
    }

    @Override
    public String toString() {
        return mX + ":" + mY;
    }

    public float toFloat() {
        return (float) mX / mY;
    }

    public CameraAspectRatio inverse() {
        return CameraAspectRatio.of(mY, mX);
    }

    @Override
    public int hashCode() {
        return mY ^ ((mX << (Integer.SIZE / 2)) | (mX >>> (Integer.SIZE / 2)));
    }

    @Override
    public int compareTo(CameraAspectRatio another) {
        if (equals(another)) {
            return 0;
        } else if (toFloat() - another.toFloat() > 0) {
            return 1;
        }
        return -1;
    }

    public static CameraAspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;
        SparseArray<CameraAspectRatio> arrayX = sCache.get(x);
        if (arrayX == null) {
            CameraAspectRatio ratio = new CameraAspectRatio(x, y);
            arrayX = new SparseArray<>();
            arrayX.put(y, ratio);
            sCache.put(x, arrayX);
            return ratio;
        } else {
            CameraAspectRatio ratio = arrayX.get(y);
            if (ratio == null) {
                ratio = new CameraAspectRatio(x, y);
                arrayX.put(y, ratio);
            }
            return ratio;
        }
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    public static final Parcelable.Creator<CameraAspectRatio> CREATOR = new Parcelable.Creator<CameraAspectRatio>() {

        @Override
        public CameraAspectRatio createFromParcel(Parcel source) {
            int x = source.readInt();
            int y = source.readInt();
            return CameraAspectRatio.of(x, y);
        }

        @Override
        public CameraAspectRatio[] newArray(int size) {
            return new CameraAspectRatio[size];
        }

    };

}