package camerakit.android.camera.core

import camerakit.android.util.CameraSize

interface CameraAttributes {

    val sensorOrientation: Int
    val previewSizes: Array<CameraSize>

}