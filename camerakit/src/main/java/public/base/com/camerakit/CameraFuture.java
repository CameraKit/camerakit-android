package com.camerakit;

public class CameraFuture<T> {

    private CameraExecutor mCameraExecutor;

    private T mData;
    private boolean mFinishedWithoutResult;
    private Throwable mError;

    private Success mSuccessCallback;
    private Data<T> mDataCallback;
    private Error mErrorCallback;

    private Then mThenCallback;
    private CameraFuture<T> mThenFuture;

    private CameraFuture() {
    }

    private CameraFuture(CameraExecutor executor) {
        mCameraExecutor = executor;
    }

    public CameraFuture(CameraExecutor executor, BaseRunnable<T> runnable) {
        mCameraExecutor = executor;
        mCameraExecutor.execute(() -> {
            try {
                runnable.run(this);
            } catch (Exception e) {
                complete(e);
            }
        });
    }

    public CameraFuture(CameraExecutor executor, CameraExecutor.BaseRunnable runnable) {
        mCameraExecutor = executor;
        mCameraExecutor.execute(new CameraExecutor.Runner(this, runnable));
    }

    private void run(CameraExecutor.BaseRunnable runnable) {
        mCameraExecutor.execute(new CameraExecutor.Runner(this, runnable));
    }

    public CameraFuture<T> then(Then callback) {
        mThenFuture = new CameraFuture<>(mCameraExecutor);

        if (mData != null || mFinishedWithoutResult) {
            mThenFuture.run(callback::then);
            return mThenFuture;
        }

        if (mError != null) {
            mThenFuture.complete(mError);
            return mThenFuture;
        }

        mThenCallback = callback;
        return mThenFuture;
    }


    public CameraFuture<T> success(Success callback) {
        if (mFinishedWithoutResult || mData != null) {
            callback.success();
            return this;
        }

        mSuccessCallback = callback;
        return this;
    }

    public CameraFuture<T> result(Data<T> callback) {
        if (mData != null) {
            callback.data(mData);
            return this;
        }

        mDataCallback = callback;
        return this;
    }

    public CameraFuture<T> error(Error callback) {
        if (mError != null) {
            callback.error(mError);
            return this;
        }

        mErrorCallback = callback;
        return this;
    }

    void complete() {
        mFinishedWithoutResult = true;
        if (mSuccessCallback != null) {
            mSuccessCallback.success();
        }

        if (mThenFuture != null && mThenCallback != null) {
            mThenFuture.run(mThenCallback::then);
        }
    }

    void complete(T result) {
        mData = result;
        if (mDataCallback != null) {
            mDataCallback.data(result);
        }

        if (mThenFuture != null && mThenCallback != null) {
            mThenFuture.run(mThenCallback::then);
        }
    }

    void complete(Throwable error) {
        mError = error;
        if (mErrorCallback != null) {
            mErrorCallback.error(error);
        }

        if (mThenFuture != null && mThenCallback != null) {
            mThenFuture.complete(error);
        }
    }

    public interface Then {
        void then();
    }

    public interface Success {
        void success();
    }

    public interface Data<T> {
        void data(T result);
    }

    public interface Error {
        void error(Throwable error);
    }

    interface BaseRunnable<T> {
        void run(CameraFuture<T> cameraFuture) throws Exception;
    }

}
