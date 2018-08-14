package camerakit.android.camera.core

interface CameraListener {

    fun onCameraError(e: Exception)
    fun onPreviewError(e: Exception)

}
