package com.camerakit;

import android.os.Handler;
import android.os.HandlerThread;

public class CameraPending<T extends CameraProduct> {

    private static Handler mHandler;
    private static HandlerThread mHandlerThread;

    private T mData;
    private DataCallback<T> mDataCallback;

    private Throwable mError;
    private ErrorCallback mErrorCallback;

    public CameraPending() {
    }

    public CameraPending(PendingProcess<T> process) {
        run(process);
    }

    public CameraPending<T> whenReady(DataCallback<T> callback) {
        mDataCallback = callback;
        if (mData != null) {
            mDataCallback.data(mData);
        }

        return this;
    }

    public CameraPending<T> catchError(ErrorCallback callback) {
        mErrorCallback = callback;
        if (mError != null) {
            mErrorCallback.error(mError);
        }

        return this;
    }

    void set(T data) {
        mData = data;
        if (mDataCallback != null) {
            mDataCallback.data(mData);
        }
    }

    void set(Throwable error) {
        mError = error;
        if (mErrorCallback != null) {
            mErrorCallback.error(mError);
        }
    }

    void run(PendingProcess<T> process) {
        async(() -> {
            try {
                process.run(this);
            } catch (Exception e) {
                set(e);
            }
        });
    }

    public interface DataCallback<T> {
        void data(T data);
    }

    public interface ErrorCallback {
        void error(Throwable error);
    }

    public interface PendingProcess<T extends CameraProduct> {
        void run(CameraPending<T> pending) throws Exception;
    }

    protected void async(Runnable runnable) {
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("ProductThread");
            mHandlerThread.start();
        }

        if (mHandler == null) {
            mHandler = new Handler(mHandlerThread.getLooper());
        }

        mHandler.post(runnable);
    }

}
