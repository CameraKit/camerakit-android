package com.camerakit.api

interface CameraEvents {

    fun onCameraOpened(cameraAttributes: CameraAttributes)
    fun onCameraClosed()
    fun onCameraError()

    fun onPreviewStarted()
    fun onPreviewStopped()
    fun onPreviewError()

}