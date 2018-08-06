package com.camerakit.api.camera2.ext

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Handler
import android.support.annotation.RequiresApi
import android.view.Surface

@RequiresApi(21)
fun CameraDevice.getCaptureSession(surface: Surface, imageReader: ImageReader, handler: Handler, callback: (captureSession: CameraCaptureSession) -> Unit) {
    createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback() {
        override fun onConfigured(captureSession: CameraCaptureSession) {
            callback(captureSession)
        }

        override fun onConfigureFailed(captureSession: CameraCaptureSession) {
            throw RuntimeException()
        }
    }, handler)
}
