package com.camerakit.api

interface CameraApi : CameraActions, CameraEvents {

    val cameraHandler: CameraHandler

}