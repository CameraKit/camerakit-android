package com.camerakit.api

import android.os.Handler
import android.os.HandlerThread

class CameraHandler private constructor(thread: HandlerThread) : Handler(thread.looper) {

    companion object {
        fun get(): CameraHandler {
            val cameraThread = HandlerThread("CameraHandler@${System.currentTimeMillis()}")
            cameraThread.start()
            return CameraHandler(cameraThread)
        }
    }

    init {
        thread.setUncaughtExceptionHandler { thread, exception ->
        }
    }

}
