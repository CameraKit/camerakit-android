package camerakit.android.camera.core

import android.os.Handler
import android.os.HandlerThread

class CameraHandler(private val handlerThread: HandlerThread) : Handler(handlerThread.looper) {

    companion object {

        operator fun invoke(): CameraHandler {
            val handlerThread = HandlerThread("CameraApi${System.currentTimeMillis()}")
            handlerThread.start()
            return CameraHandler(handlerThread)
        }

    }

    fun postNormalPriority(runnable: Runnable) {
        post {
            handlerThread.priority = Thread.NORM_PRIORITY
            runnable.run()
            resetPriority()
        }
    }

    fun postLowPriority(runnable: Runnable) {
        post {
            handlerThread.priority = Thread.MIN_PRIORITY
            runnable.run()
            resetPriority()
        }
    }

    fun postHighPriority(runnable: Runnable) {
        post {
            handlerThread.priority = Thread.MAX_PRIORITY
            runnable.run()
            resetPriority()
        }
    }

    fun postNow(runnable: Runnable) {
        postAtFrontOfQueue {
            handlerThread.priority = Thread.MAX_PRIORITY
            runnable.run()
            resetPriority()
        }
    }

    private fun resetPriority() {
        handlerThread.priority = Thread.NORM_PRIORITY
    }

}
