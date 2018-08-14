package camerakit.android.camera

class CameraException : Exception {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(throwable: Throwable) : super(throwable)
    constructor(message: String, throwable: Throwable) : super(message, throwable)

}