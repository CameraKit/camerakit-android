package com.camerakit;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.Executor;

class CameraExecutor implements Executor {

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    CameraExecutor() {
        super();
    }

    void start() {
        if (mHandler != null || mHandlerThread != null) {
            stop();
        }

        mHandlerThread = new HandlerThread("CameraApi");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    void stop() {
        killThread(mHandlerThread);
        mHandlerThread = null;
        mHandler = null;
    }

    private void killThread(HandlerThread thread) {
        if (thread == null) return;
        if (Build.VERSION.SDK_INT >= 18) {
            thread.quitSafely();
        } else {
            thread.quit();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e("CameraKit", e.toString());
        }
    }

    @Override
    public synchronized void execute(final Runnable task) {
        mHandler.post(task);
    }

    public synchronized void execute(final Runner runner) {
        mHandler.post(runner.mRunnable);
    }

    interface BaseRunnable {
        void run() throws Throwable;
    }

    interface ResultRunnable<T> {
        T run() throws Throwable;
    }

    static class Runner<T> {

        Runnable mRunnable;

        private Runner() {
        }

        Runner(CameraFuture future, BaseRunnable runnable) {
            mRunnable = () -> {
              try {
                  runnable.run();
                  future.complete();
              } catch (Throwable throwable) {
                  future.complete(throwable);
              }
            };
        }

        Runner(CameraFuture<T> future, ResultRunnable<T> resultRunnable) {
            mRunnable = () -> {
                try {
                    T result = resultRunnable.run();
                    future.complete(result);
                } catch (Throwable throwable) {
                    future.complete(throwable);
                }
            };
        }

    }

}
