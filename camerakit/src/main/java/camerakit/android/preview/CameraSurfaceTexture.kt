package camerakit.android.preview

import android.graphics.SurfaceTexture
import android.support.annotation.Keep
import camerakit.android.util.CameraSize

class CameraSurfaceTexture(inputTexture: Int, val outputTexture: Int) : SurfaceTexture(inputTexture) {

    var size: CameraSize = CameraSize(0, 0)
        set(size) {
            field = size
            previewInvalidated = true
        }

    private var previewInvalidated = false
    private val transformMatrix: FloatArray = FloatArray(16)

    init {
        nativeInit(inputTexture, outputTexture)
    }

    override fun updateTexImage() {
        if (previewInvalidated) {
            nativeSetSize(size.width, size.height)
            previewInvalidated = false
        }

        super.updateTexImage()
        getTransformMatrix(transformMatrix)
        nativeUpdateTexImage(transformMatrix)
    }

    override fun release() {
        nativeRelease()
    }

    // ---

    override fun finalize() {
        super.finalize()
        try {
            nativeFinalize()
        } catch (e: Exception) {
            // ignore
        }
    }

    // ---

    @Keep
    private var nativeHandle: Long = 0L

    private external fun nativeInit(inputTexture: Int, outputTexture: Int)

    private external fun nativeSetSize(width: Int, height: Int)

    private external fun nativeUpdateTexImage(transformMatrix: FloatArray)

    private external fun nativeFinalize()

    private external fun nativeRelease()

    companion object {

        init {
            System.loadLibrary("camerakit")
        }

    }

}

