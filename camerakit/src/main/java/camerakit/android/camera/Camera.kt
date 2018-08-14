package camerakit.android.camera

import android.content.Context
import android.os.Build
import camerakit.android.camera.core.CameraApi
import camerakit.android.camera.core.CameraAttributes
import camerakit.android.camera.core.CameraListener
import camerakit.android.camera.core.v1.Camera1
import camerakit.android.camera.core.v2.Camera2
import camerakit.android.preview.CameraSurfaceTexture
import camerakit.android.util.CameraSize
import kotlin.coroutines.experimental.suspendCoroutine

class Camera(context: Context) : CameraListener {

    companion object {

        /** Force Camera1 even on API 21+ */
        private const val FORCE_DEPRECATED_API = false

    }

    private val cameraApi: CameraApi

    init {
        @SuppressWarnings("NewApi")
        cameraApi = when (Build.VERSION.SDK_INT < 21 || FORCE_DEPRECATED_API) {
            true -> Camera1(this)
            false -> Camera2(context, this)
        }
    }

    @Synchronized
    @Throws(CameraException::class)
    suspend fun startCamera(): CameraAttributes = suspendCoroutine { continuation ->
        cameraApi.openCamera(object : CameraApi.CameraOpenCallback {
            override fun onOpened(cameraAttributes: CameraAttributes) {
                continuation.resume(cameraAttributes)
            }

            override fun onError() {
                val exception = CameraException()
                continuation.resumeWithException(exception)
            }
        })
    }

    @Synchronized
    @Throws(CameraException::class)
    suspend fun stopCamera(): Unit = suspendCoroutine { continuation ->
        cameraApi.closeCamera(object : CameraApi.CameraCloseCallback {
            override fun onClose() {
                continuation.resume(Unit)
            }

            override fun onError() {
                val exception = CameraException()
                continuation.resumeWithException(exception)
            }
        })
    }

    @Synchronized
    @Throws(CameraException::class)
    suspend fun startPreview(cameraSurfaceTexture: CameraSurfaceTexture,
                             previewSize: CameraSize,
                             previewOrientation: Int): Unit = suspendCoroutine { continuation ->
        cameraSurfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        cameraSurfaceTexture.size = when (previewOrientation % 180) {
            0 -> previewSize
            else -> CameraSize(previewSize.height, previewSize.width)
        }

        cameraApi.startPreview(cameraSurfaceTexture, previewSize, previewOrientation,
                object : CameraApi.PreviewStartCallback {
                    override fun onStarted() {
                        continuation.resume(Unit)
                    }

                    override fun onError() {
                        val exception = CameraException()
                        continuation.resumeWithException(exception)
                    }
                })
    }

    @Synchronized
    @Throws(CameraException::class)
    suspend fun stopPreview(): Unit = suspendCoroutine { continuation ->
        cameraApi.stopPreview(object : CameraApi.PreviewStopCallback {
            override fun onStop() {
                continuation.resume(Unit)
            }

            override fun onError() {
                val exception = CameraException()
                continuation.resumeWithException(exception)
            }
        })
    }

    @Synchronized
    @Throws(CameraException::class)
    suspend fun capture(): ByteArray = suspendCoroutine { continuation ->
    }

    // CameraListener:

    override fun onCameraError(e: Exception) {
    }

    override fun onPreviewError(e: Exception) {
    }

}
