package camerakit.android.camera.core.v2

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.SurfaceHolder
import camerakit.android.camera.core.CameraApi
import camerakit.android.camera.core.CameraAttributes
import camerakit.android.camera.core.CameraListener
import camerakit.android.util.CameraSize

@RequiresApi(21)
@SuppressWarnings("MissingPermission")
class Camera2(private val cameraManager: CameraManager,
              private val cameraListener: CameraListener) : CameraApi() {

    companion object {
        operator fun invoke(context: Context, cameraListener: CameraListener): Camera2 {
            val cameraManager: CameraManager =
                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            return Camera2(cameraManager, cameraListener)
        }
    }

    private var cameraDevice: CameraDevice? = null
    private var previewCaptureSession: CameraCaptureSession? = null
    private var previewCaptureRequest: CaptureRequest? = null

    override fun openCamera(callback: CameraOpenCallback) {
        handler(Priority.BACKGROUND) {
            val currentCameraDevice = cameraDevice
            if (currentCameraDevice != null) {
                callback.onError()
                return@handler
            }

            val cameraId = cameraManager.cameraIdList.find {
                cameraManager.getCameraCharacteristics(it)
                        .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }

            if (cameraId == null) {
                callback.onError()
                return@handler
            }

            checkCameraAvailability(cameraId) { isAvailable ->
                if (!isAvailable) {
                    callback.onError()
                } else {
                    getCameraDevice(cameraId, cameraListener::onCameraError) { camera, success ->
                        if (success) {
                            cameraDevice = camera
                            callback.onOpened(Attributes(cameraManager.getCameraCharacteristics(cameraId)))
                        } else {
                            camera.close()
                            callback.onError()
                        }
                    }
                }
            }
        }
    }

    private fun checkCameraAvailability(targetCameraId: String, callback: ((isAvailable: Boolean) -> Unit)) {
        cameraManager.registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
            override fun onCameraAvailable(cameraId: String) {
                if (cameraId == targetCameraId) {
                    cameraManager.unregisterAvailabilityCallback(this)
                    callback(true)
                }
            }

            override fun onCameraUnavailable(cameraId: String) {
                if (cameraId == targetCameraId) {
                    cameraManager.unregisterAvailabilityCallback(this)
                    callback(false)
                }
            }
        }, null)
    }

    private fun getCameraDevice(cameraId: String, errorListener: ((e: Exception) -> Unit),
                                callback: ((camera: CameraDevice, success: Boolean) -> Unit)) {
        var callbackSpent = false
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                callbackSpent = true
                callback(camera, true)
            }

            override fun onDisconnected(camera: CameraDevice) {
                if (!callbackSpent) {
                    callbackSpent = true
                    callback(camera, false)
                } else {
                    errorListener(RuntimeException("CameraDevice became disconnected!"))
                }
            }

            override fun onError(camera: CameraDevice, error: Int) {
                errorListener(when (error) {
                    CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> RuntimeException("ERROR_CAMERA_IN_USE")
                    CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> RuntimeException("ERROR_MAX_CAMERAS_IN_USE")
                    CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> RuntimeException("ERROR_CAMERA_DISABLED")
                    CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> RuntimeException("ERROR_CAMERA_DEVICE")
                    CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> RuntimeException("ERROR_CAMERA_SERVICE")
                    else -> RuntimeException("Undefined camera error received, but assuming fatal.")
                })
            }
        }, null)
    }

    override fun closeCamera(callback: CameraCloseCallback) {
        handler(Priority.IMMEDIATE) {
            val currentCameraDevice = cameraDevice
            if (currentCameraDevice == null) {
                callback.onError()
                return@handler
            }

            try {
                currentCameraDevice.close()
            } finally {
                cameraDevice = null
                callback.onClose()
            }
        }
    }

    override fun startPreview(surfaceTexture: SurfaceTexture,
                              previewSize: CameraSize,
                              displayOrientation: Int,
                              callback: PreviewStartCallback) {
        handler {
            val cameraDevice = cameraDevice
            if (cameraDevice == null) {
                callback.onError()
                return@handler
            }

            val captureSession = previewCaptureSession
            if (captureSession != null) {
                callback.onError()
                return@handler
            }

            val surface = Surface(surfaceTexture)
            getCaptureSession(cameraDevice, surface) { captureSession, success ->
                if (success) {
                    try {
                        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        captureRequestBuilder.addTarget(surface)
                        bindCaptureRequest(captureSession, captureRequestBuilder.build()) { captureRequest, bindSuccess ->
                            if (bindSuccess) {
                                previewCaptureSession = captureSession
                                previewCaptureRequest = captureRequest
                                callback.onStarted()
                            } else {
                                callback.onError()
                            }
                        }
                    } catch (e: Exception) {
                        callback.onError()
                    }
                } else {
                    callback.onError()
                }
            }
        }
    }

    private fun getCaptureSession(cameraDevice: CameraDevice, surface: Surface,
                                  callback: ((captureSession: CameraCaptureSession, success: Boolean) -> Unit)) {
        cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                callback(session, true)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                callback(session, false)
            }
        }, null)
    }

    private fun bindCaptureRequest(captureSession: CameraCaptureSession, captureRequest: CaptureRequest,
                                   callback: ((captureRequest: CaptureRequest, success: Boolean) -> Unit)) {
        try {
            var callbackSpent = false
            captureSession.setRepeatingRequest(captureRequest, object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
                    if (!callbackSpent) {
                        callbackSpent = true
                        callback(request, true)
                    }
                }
            }, null)
        } catch (e: Exception) {
            callback(captureRequest, false)
        }

    }


    override fun stopPreview(callback: PreviewStopCallback) {
        handler(Priority.IMMEDIATE) {
            val currentCaptureSession = previewCaptureSession
            if (currentCaptureSession == null) {
                callback.onError()
                return@handler
            }

            try {
                currentCaptureSession.stopRepeating()
                currentCaptureSession.close()
            } finally {
                previewCaptureSession = null
                callback.onStop()
            }
        }
    }

    private class Attributes(cameraCharacteristics: CameraCharacteristics) : CameraAttributes {

        override val sensorOrientation: Int = cameraCharacteristics.getSensorOrientation()

        override val previewSizes: Array<CameraSize> = cameraCharacteristics.getPreviewSizes()


        // KTX Helpers:

        private fun CameraCharacteristics.getSensorOrientation(): Int {
            return get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
        }

        private fun CameraCharacteristics.getPreviewSizes(): Array<CameraSize> {
            val streamConfigMap = get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: return emptyArray()

            val outputSizes = streamConfigMap.getOutputSizes(SurfaceHolder::class.java)
                    ?: return emptyArray()

            return outputSizes.map { CameraSize(it) }.toTypedArray()
        }

    }

}
