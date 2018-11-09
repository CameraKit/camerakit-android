package com.camerakit.preview

import android.graphics.SurfaceTexture
import android.opengl.Matrix
import androidx.annotation.Keep
import com.camerakit.type.CameraSize

class CameraSurfaceTexture(inputTexture: Int, val outputTexture: Int) : SurfaceTexture(inputTexture) {

    var size: CameraSize = CameraSize(0, 0)
        set(size) {
            field = size
            previewInvalidated = true
        }

    private var previewInvalidated = false
    private val transformMatrix: FloatArray = FloatArray(16)
    private val extraTransformMatrix: FloatArray = FloatArray(16)

    init {
        nativeInit(inputTexture, outputTexture)
        Matrix.setIdentityM(extraTransformMatrix, 0)
    }

    override fun updateTexImage() {
        if (previewInvalidated) {
            nativeSetSize(size.width, size.height)
            previewInvalidated = false
        }

        super.updateTexImage()
        getTransformMatrix(transformMatrix)
        nativeUpdateTexImage(transformMatrix, extraTransformMatrix)
    }

    override fun release() {
        nativeRelease()
    }

    fun setRotation(degrees: Int) {
        Matrix.setIdentityM(extraTransformMatrix, 0)
        Matrix.rotateM(extraTransformMatrix, 0, degrees.toFloat(), 0f, 0f, 1f)
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

    private external fun nativeUpdateTexImage(transformMatrix: FloatArray, extraTransformMatrix: FloatArray)

    private external fun nativeFinalize()

    private external fun nativeRelease()

    companion object {

        init {
            System.loadLibrary("camerakit")
        }

    }

}

