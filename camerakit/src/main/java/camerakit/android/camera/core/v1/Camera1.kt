package camerakit.android.camera.core.v1

import android.graphics.SurfaceTexture
import android.hardware.Camera
import camerakit.android.camera.core.CameraApi
import camerakit.android.camera.core.CameraAttributes
import camerakit.android.camera.core.CameraListener
import camerakit.android.util.CameraSize

@SuppressWarnings("deprecation")
class Camera1(private val cameraListener: CameraListener) : CameraApi() {

    private var camera: Camera? = null

    override fun openCamera(callback: CameraOpenCallback) {
        handler(Priority.BACKGROUND) {
            val currentCamera = camera
            if (currentCamera != null) {
                callback.onError()
                return@handler
            }

            try {
                val cameraInfo = Camera.CameraInfo()
                val cameraId = (0 until Camera.getNumberOfCameras()).find {
                    Camera.getCameraInfo(it, cameraInfo)
                    cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                }

                if (cameraId == null) {
                    callback.onError()
                    return@handler
                }

                val camera = Camera.open(cameraId)
                camera.setErrorCallback { error, _ ->
                    cameraListener.onCameraError(when (error) {
                        Camera.CAMERA_ERROR_EVICTED -> RuntimeException("CAMERA_ERROR_EVICTED")
                        Camera.CAMERA_ERROR_SERVER_DIED -> RuntimeException("CAMERA_ERROR_SERVER_DIED")
                        Camera.CAMERA_ERROR_UNKNOWN -> RuntimeException("CAMERA_ERROR_UNKNOWN")
                        else -> RuntimeException("Undefined camera error received, but assuming fatal.")
                    })
                }

                this.camera = camera
                callback.onOpened(Attributes(cameraInfo, camera.parameters))
            } catch (e: Exception) {
                callback.onError()
            }
        }
    }

    override fun closeCamera(callback: CameraCloseCallback) {
        handler(Priority.IMMEDIATE) {
            val currentCamera = camera
            if (currentCamera == null) {
                callback.onError()
                return@handler
            }

            try {
                currentCamera.release()
            } finally {
                camera = null
                callback.onClose()
            }
        }
    }

    override fun startPreview(surfaceTexture: SurfaceTexture,
                              previewSize: CameraSize,
                              displayOrientation: Int,
                              callback: PreviewStartCallback) {
        handler {
            val camera = camera
            if (camera == null) {
                callback.onError()
                return@handler
            }

            try {
                val parameters = camera.parameters
                parameters.setPreviewSize(previewSize.width, previewSize.height)
                camera.parameters = parameters

                camera.setDisplayOrientation(displayOrientation)
                camera.setPreviewTexture(surfaceTexture)
                camera.setOneShotPreviewCallback { _, _ ->
                    callback.onStarted()
                }
                camera.startPreview()
            } catch (e: Exception) {
                callback.onError()
            }
        }
    }

    override fun stopPreview(callback: PreviewStopCallback) {
        handler(Priority.IMMEDIATE) {
            val camera = camera
            if (camera == null) {
                callback.onError()
                return@handler
            }

            try {
                camera.stopPreview()
            } finally {
                callback.onStop()
            }
        }
    }

    private class Attributes(cameraInfo: Camera.CameraInfo,
                             cameraParameters: Camera.Parameters) : CameraAttributes {

        override val sensorOrientation: Int = cameraInfo.orientation

        override val previewSizes: Array<CameraSize> = cameraParameters.getPreviewSizes()


        // KTX Helpers:

        private fun Camera.Parameters.getPreviewSizes(): Array<CameraSize> {
            return supportedPreviewSizes.map { CameraSize(it) }.toTypedArray()
        }

    }

}