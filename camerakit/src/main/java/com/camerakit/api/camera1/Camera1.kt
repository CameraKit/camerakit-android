package com.camerakit.api.camera1

import android.graphics.SurfaceTexture
import android.hardware.Camera
import com.camerakit.api.CameraApi
import com.camerakit.api.CameraAttributes
import com.camerakit.api.CameraEvents
import com.camerakit.api.CameraHandler
import com.camerakit.api.camera1.ext.getFlashes
import com.camerakit.api.camera1.ext.getPhotoSizes
import com.camerakit.api.camera1.ext.getPreviewSizes
import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize

class Camera1(eventsDelegate: CameraEvents) :
        CameraApi, CameraEvents by eventsDelegate {

    override val cameraHandler: CameraHandler = CameraHandler.get()

    private var camera: Camera? = null
    private var cameraAttributes: CameraAttributes? = null

    @Synchronized
    override fun open(facing: CameraFacing) {
        val cameraId = when (facing) {
            CameraFacing.BACK -> Camera.CameraInfo.CAMERA_FACING_BACK
            CameraFacing.FRONT -> Camera.CameraInfo.CAMERA_FACING_FRONT
        }

        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraId) {
                val camera = Camera.open(i)
                val cameraParameters = camera.parameters
                val cameraAttributes = Attributes(cameraInfo, cameraParameters, facing)

                this.camera = camera
                this.cameraAttributes = cameraAttributes
                onCameraOpened(cameraAttributes)
            }
        }
    }

    @Synchronized
    override fun release() {
        camera?.release()
        camera = null
        cameraAttributes = null
        onCameraClosed()
    }

    @Synchronized
    override fun setPreviewOrientation(degrees: Int) {
        val camera = camera
        if (camera != null) {
            camera.setDisplayOrientation(degrees)
        }
    }

    @Synchronized
    override fun setPreviewSize(size: CameraSize) {
        val camera = camera
        if (camera != null) {
            val parameters = camera.parameters
            parameters.setPreviewSize(size.width, size.height)
            camera.parameters = parameters
        }
    }

    @Synchronized
    override fun startPreview(surfaceTexture: SurfaceTexture) {
        val camera = camera
        if (camera != null) {
            val parameters = camera.parameters
            if (parameters.supportedFocusModes != null && Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE in parameters.supportedFocusModes) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                camera.parameters = parameters
            }

            camera.setPreviewTexture(surfaceTexture)
            camera.setOneShotPreviewCallback { _, _ ->
                onPreviewStarted()
            }
            camera.startPreview()
        }
    }

    @Synchronized
    override fun stopPreview() {
        val camera = camera
        if (camera != null) {
            camera.stopPreview()
            onPreviewStopped()
        }
    }

    @Synchronized
    override fun setFlash(flash: CameraFlash) {
        val camera = camera
        if (camera != null) {
            val parameters = camera.parameters
            parameters.flashMode = when(flash) {
                CameraFlash.OFF -> Camera.Parameters.FLASH_MODE_OFF
                CameraFlash.ON -> Camera.Parameters.FLASH_MODE_ON
                CameraFlash.AUTO -> Camera.Parameters.FLASH_MODE_AUTO
                CameraFlash.TORCH -> Camera.Parameters.FLASH_MODE_TORCH
            }

            try {
                camera.parameters = parameters
            } catch (e: Exception) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Synchronized
    override fun setPhotoSize(size: CameraSize) {
        val camera = camera
        if (camera != null) {
            val parameters = camera.parameters
            parameters.setPictureSize(size.width, size.height)

            try {
                camera.parameters = parameters
            } catch (e: Exception) {
                // ignore failures for minor parameters like this for now
            }
        }
    }

    @Synchronized
    override fun capturePhoto(callback: (jpeg: ByteArray) -> Unit) {
        val camera = camera
        if (camera != null) {
            camera.takePicture(null, null) { data, _ ->
                callback(data)
                camera.startPreview()
            }
        }
    }

    private class Attributes(cameraInfo: Camera.CameraInfo,
                             cameraParameters: Camera.Parameters,
                             cameraFacing: CameraFacing) : CameraAttributes {

        override val facing: CameraFacing = cameraFacing

        override val sensorOrientation: Int = cameraInfo.orientation

        override val previewSizes: Array<CameraSize> = cameraParameters.getPreviewSizes()

        override val photoSizes: Array<CameraSize> = cameraParameters.getPhotoSizes()

        override val flashes: Array<CameraFlash> = cameraParameters.getFlashes()
    }

}