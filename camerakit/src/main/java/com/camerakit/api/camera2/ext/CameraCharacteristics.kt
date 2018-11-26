package com.camerakit.api.camera2.ext

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.RequiresApi
import android.view.SurfaceHolder
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize

@RequiresApi(21)
fun CameraCharacteristics.getSensorOrientation(): Int {
    return get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
}

@RequiresApi(21)
fun CameraCharacteristics.getPreviewSizes(): Array<CameraSize> {
    val streamConfigMap = get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: return emptyArray()

    val outputSizes = streamConfigMap.getOutputSizes(SurfaceHolder::class.java)
            ?: return emptyArray()

    return outputSizes
            .map { CameraSize(it.width, it.height) }
            .toTypedArray()
}

@RequiresApi(21)
fun CameraCharacteristics.getPhotoSizes(): Array<CameraSize> {
    val streamConfigMap = get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: return emptyArray()

    val outputSizes = streamConfigMap.getOutputSizes(ImageFormat.JPEG)
            ?: return emptyArray()

    return outputSizes
            .map { CameraSize(it.width, it.height) }
            .toTypedArray()
}

@RequiresApi(21)
fun CameraCharacteristics.getFlashes(): Array<CameraFlash> {
    val flashSupported = get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
    if (flashSupported) {
        return arrayOf(CameraFlash.OFF, CameraFlash.ON, CameraFlash.AUTO, CameraFlash.TORCH)
    } else {
        return emptyArray()
    }
}

