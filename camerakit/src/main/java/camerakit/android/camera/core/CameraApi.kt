package camerakit.android.camera.core

import android.graphics.SurfaceTexture
import camerakit.android.util.CameraSize

abstract class CameraApi {

    // Async/threading for protected use by implementations of CameraApi:

    private val cameraHandler: CameraHandler = CameraHandler()

    protected enum class Priority {
        NORMAL,
        IMMEDIATE,
        FOREGROUND,
        BACKGROUND;
    }

    protected fun handler(priority: Priority = Priority.NORMAL, task: (() -> Unit)) {
        when (priority) {
            Priority.NORMAL -> cameraHandler.postNormalPriority(Runnable(task))
            Priority.IMMEDIATE -> cameraHandler.postNow(Runnable(task))
            Priority.FOREGROUND -> cameraHandler.postHighPriority(Runnable(task))
            Priority.BACKGROUND -> cameraHandler.postLowPriority(Runnable(task))
        }
    }


    // CameraApi:

    interface CameraOpenCallback {

        fun onOpened(cameraAttributes: CameraAttributes)

        fun onError()

    }

    abstract fun openCamera(callback: CameraOpenCallback)

    interface CameraCloseCallback {

        fun onClose()

        fun onError()

    }

    abstract fun closeCamera(callback: CameraCloseCallback)


    interface PreviewStartCallback {

        fun onStarted()

        fun onError()

    }

    abstract fun startPreview(surfaceTexture: SurfaceTexture,
                              previewSize: CameraSize,
                              displayOrientation: Int,
                              callback: PreviewStartCallback)

    interface PreviewStopCallback {

        fun onStop()

        fun onError()

    }

    abstract fun stopPreview(callback: PreviewStopCallback)

}
