package com.camerakit.api

import android.graphics.SurfaceTexture
import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize

interface CameraActions {

    fun open(facing: CameraFacing)
    fun release()

    fun setPreviewOrientation(degrees: Int)
    fun setPreviewSize(size: CameraSize)
    fun startPreview(surfaceTexture: SurfaceTexture)
    fun stopPreview()

    fun setFlash(flash: CameraFlash)
    fun setPhotoSize(size: CameraSize)
    fun capturePhoto(callback: (jpeg: ByteArray) -> Unit)


}