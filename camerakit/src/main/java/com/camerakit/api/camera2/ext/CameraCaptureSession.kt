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

interface LockFocusCallback {
    fun preCapture()
    fun shutter()
    fun capture()
}

@RequiresApi(21)
fun CameraCaptureSession.lockFocus(previewRequest: CaptureRequest, handler: Handler, callback: LockFocusCallback) {
    var focusCallback: LockFocusCallback? = callback
    capture(previewRequest, object : CameraCaptureSession.CaptureCallback() {
        fun checkResult(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                focusCallback?.capture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    focusCallback?.capture()
                    focusCallback = null
                } else {
                    focusCallback?.preCapture()
                    focusCallback = null
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            checkResult(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            checkResult(result)
        }
    }, handler)
}

@RequiresApi(21)
fun CameraCaptureSession.preCapture(previewRequest: CaptureRequest, handler: Handler, callback: () -> Unit) {
    var preCaptureCallback: (() -> Unit)? = callback
    capture(previewRequest, object : CameraCaptureSession.CaptureCallback() {
        private var waitingPreCapture = true
        private var waitingNonPreCapture = false

        fun checkResult(result: CaptureResult) {
            if (waitingPreCapture) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    waitingNonPreCapture = true
                }
            } else if (waitingNonPreCapture) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    preCaptureCallback?.invoke()
                    preCaptureCallback = null
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            checkResult(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            checkResult(result)
        }
    }, handler)
}
