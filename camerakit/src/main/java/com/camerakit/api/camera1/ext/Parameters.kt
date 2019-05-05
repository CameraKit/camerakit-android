package com.camerakit.api.camera1.ext

import android.hardware.Camera
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize

fun Camera.Parameters.getPreviewSizes(): Array<CameraSize> {
    return supportedPreviewSizes
            .map { CameraSize(it.width, it.height) }
            .toTypedArray()
}

fun Camera.Parameters.getPhotoSizes(): Array<CameraSize> {
    return supportedPictureSizes
            .map { CameraSize(it.width, it.height) }
            .toTypedArray()
}

fun Camera.Parameters.getFlashes(): Array<CameraFlash> {
    if (supportedFlashModes == null) {
        return emptyArray()
    }

    return supportedFlashModes
            .filter {
                when (it) {
                    Camera.Parameters.FLASH_MODE_OFF -> true
                    Camera.Parameters.FLASH_MODE_ON -> true
                    Camera.Parameters.FLASH_MODE_AUTO -> true
                    Camera.Parameters.FLASH_MODE_TORCH -> true
                    else -> false
                }
            }.map {
                when (it) {
                    Camera.Parameters.FLASH_MODE_OFF -> CameraFlash.OFF
                    Camera.Parameters.FLASH_MODE_ON -> CameraFlash.ON
                    Camera.Parameters.FLASH_MODE_AUTO -> CameraFlash.AUTO
                    Camera.Parameters.FLASH_MODE_TORCH -> CameraFlash.TORCH
                    else -> CameraFlash.OFF
                }
            }.toTypedArray()
}
