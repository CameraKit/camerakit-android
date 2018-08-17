package com.camerakit.api.camera2

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.support.annotation.RequiresApi
import android.view.Surface
import com.camerakit.api.CameraApi
import com.camerakit.api.CameraAttributes
import com.camerakit.api.CameraEvents
import com.camerakit.api.CameraHandler
import com.camerakit.api.camera2.ext.*
import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize


@RequiresApi(21)
@SuppressWarnings("MissingPermission")
class Camera2(eventsDelegate: CameraEvents, context: Context) :
        CameraApi, CameraEvents by eventsDelegate {

    override val cameraHandler: CameraHandler = CameraHandler.get()

    private val cameraManager: CameraManager =
            context.getSystemService(CAMERA_SERVICE) as CameraManager

    private var cameraDevice: CameraDevice? = null
    private var cameraAttributes: CameraAttributes? = null

    private var captureSession: CameraCaptureSession? = null

    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    private var imageReader: ImageReader? = null
    private var photoCallback: ((jpeg: ByteArray) -> Unit)? = null

    private var flash: CameraFlash = CameraFlash.OFF

    @Synchronized
    override fun open(facing: CameraFacing) {
        val cameraId = cameraManager.getCameraId(facing) ?: throw RuntimeException()
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
        cameraManager.whenDeviceAvailable(cameraId, cameraHandler) {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    val cameraAttributes = Attributes(cameraCharacteristics, facing)
                    this@Camera2.cameraDevice = cameraDevice
                    this@Camera2.cameraAttributes = cameraAttributes
                    onCameraOpened(cameraAttributes)
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {
                    cameraDevice.close()
                    this@Camera2.cameraDevice = null
                    onCameraClosed()
                }

                override fun onError(cameraDevice: CameraDevice, error: Int) {
                    cameraDevice.close()
                    this@Camera2.cameraDevice = null

                }
            }, cameraHandler)
        }
    }

    @Synchronized
    override fun release() {
        cameraDevice?.close()
        cameraDevice = null
        captureSession?.close()
        captureSession = null
        cameraAttributes = null
        imageReader?.close()
        imageReader = null
        onCameraClosed()
    }

    @Synchronized
    override fun setPreviewSize(size: CameraSize) {
    }

    @Synchronized
    override fun startPreview(surfaceTexture: SurfaceTexture) {
        val cameraDevice = cameraDevice
        val imageReader = imageReader
        if (cameraDevice != null && imageReader != null) {
            val surface = Surface(surfaceTexture)
            cameraDevice.getCaptureSession(surface, imageReader, cameraHandler) { captureSession ->
                this.captureSession = captureSession

                if (captureSession != null) {
                    val previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    previewRequestBuilder.addTarget(surface)
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    setFlashRequest(previewRequestBuilder)

                    this.captureSession?.preview(previewRequestBuilder.build(), cameraHandler) { previewRequest ->
                        this.previewRequestBuilder = previewRequestBuilder
                        this.previewRequest = previewRequest
                        onPreviewStarted()
                    }
                }
            }
        }
    }

    @Synchronized
    override fun stopPreview() {
        val captureSession = captureSession
        this.captureSession = null
        if (captureSession != null) {
            captureSession.stopRepeating()
            captureSession.abortCaptures()
            captureSession.close()
            onPreviewStopped()
        }
    }

    @Synchronized
    override fun setFlash(flash: CameraFlash) {
        this.flash = flash
    }

    private fun setFlashRequest(captureRequest: CaptureRequest.Builder) {
        captureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        if (flash == CameraFlash.OFF) {
            captureRequest.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        } else if (flash == CameraFlash.ON) {
            captureRequest.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH)
        } else if (flash == CameraFlash.AUTO) {
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    @Synchronized
    override fun setPhotoSize(size: CameraSize) {
        this.imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 4)
    }

    @Synchronized
    override fun capturePhoto(callback: (jpeg: ByteArray) -> Unit) {
        this.photoCallback = callback
        lockFocus()
    }

    private fun lockFocus() {
        val previewRequestBuilder = previewRequestBuilder
        val captureSession = captureSession
        if (previewRequestBuilder != null && captureSession != null) {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            setFlashRequest(previewRequestBuilder)
            captureState = STATE_WAITING_LOCK
            captureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler)
        }
    }

    private fun runPreCaptureSequence() {
        val previewRequestBuilder = previewRequestBuilder
        val captureSession = captureSession
        if (previewRequestBuilder != null && captureSession != null) {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            captureState = STATE_WAITING_PRECAPTURE
            captureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler)
        }
    }

    private fun captureStillPicture() {
        val captureSession = captureSession
        val cameraDevice = cameraDevice
        val imageReader = imageReader
        if (captureSession != null && cameraDevice != null && imageReader != null) {
            val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            setFlashRequest(captureBuilder)
            if (flash == CameraFlash.ON) {
                captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE)
            }

            captureSession.stopRepeating()
            captureSession.abortCaptures()
            captureSession.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    unlockFocus()
                }
            }, cameraHandler)
        }
    }

    private fun unlockFocus() {
        val previewRequestBuilder = previewRequestBuilder
        val captureSession = captureSession
        if (previewRequestBuilder != null && captureSession != null) {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setFlashRequest(previewRequestBuilder)
            captureSession.capture(previewRequestBuilder.build(), captureCallback, cameraHandler)
            captureState = STATE_PREVIEW
            captureSession.setRepeatingRequest(previewRequest, captureCallback, cameraHandler)
        }
    }

    private var captureState: Int = STATE_PREVIEW

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (captureState) {
                STATE_PREVIEW -> {
                    val image = imageReader?.acquireLatestImage()
                    if (image != null) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        photoCallback?.invoke(bytes)
                        photoCallback = null
                        image.close()
                    }
                }
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            captureState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPreCaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        captureState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        captureState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            process(result)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            process(partialResult)
        }

    }

    companion object {
        private const val STATE_PREVIEW = 0
        private const val STATE_WAITING_LOCK = 1
        private const val STATE_WAITING_PRECAPTURE = 2
        private const val STATE_WAITING_NON_PRECAPTURE = 3
        private const val STATE_PICTURE_TAKEN = 4
    }

    private class Attributes(cameraCharacteristics: CameraCharacteristics,
                             cameraFacing: CameraFacing) : CameraAttributes {

        override val facing: CameraFacing = cameraFacing

        override val sensorOrientation: Int = cameraCharacteristics.getSensorOrientation()

        override val previewSizes: Array<CameraSize> = cameraCharacteristics.getPreviewSizes()

        override val photoSizes: Array<CameraSize> = cameraCharacteristics.getPhotoSizes()

        override val flashes: Array<CameraFlash> = cameraCharacteristics.getFlashes()

    }

}