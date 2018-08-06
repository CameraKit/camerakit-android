package com.camerakit.api.camera2.ext

import android.hardware.camera2.*
import android.os.Handler
import android.support.annotation.RequiresApi

@RequiresApi(21)
fun CameraCaptureSession.preview(previewRequest: CaptureRequest, handler: Handler, callback: (request: CaptureRequest) -> Unit) {
    var previewCallback: ((request: CaptureRequest) -> Unit)? = callback
    setRepeatingRequest(previewRequest, object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            previewCallback?.invoke(request)
            previewCallback = null
        }
    }, handler)
}
