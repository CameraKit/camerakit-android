package com.camerakit.api.camera2.ext

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import androidx.annotation.RequiresApi
import com.camerakit.type.CameraFacing

@RequiresApi(21)
fun CameraManager.getCameraId(facing: CameraFacing): String? {
    val targetFacingCharacteristic = when (facing) {
        CameraFacing.BACK -> CameraCharacteristics.LENS_FACING_BACK
        CameraFacing.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
    }

    return cameraIdList.find { cameraId ->
        val characteristics = getCameraCharacteristics(cameraId)
        val facingCharacteristic = characteristics.get(CameraCharacteristics.LENS_FACING)
        facingCharacteristic == targetFacingCharacteristic
    }
}

@RequiresApi(21)
fun CameraManager.whenDeviceAvailable(targetCameraId: String, handler: Handler, callback: () -> Unit) {
    registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
        override fun onCameraAvailable(cameraId: String) {
            if (cameraId == targetCameraId) {
                unregisterAvailabilityCallback(this)
                callback()
            }
        }

        override fun onCameraUnavailable(cameraId: String) {
            if (cameraId == targetCameraId) {
            }
        }
    }, handler)
}
