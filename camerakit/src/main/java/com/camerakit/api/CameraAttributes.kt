package com.camerakit.api

import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize

interface CameraAttributes {

    val facing: CameraFacing
    val sensorOrientation: Int
    val previewSizes: Array<CameraSize>
    val photoSizes: Array<CameraSize>
    val flashes: Array<CameraFlash>

}