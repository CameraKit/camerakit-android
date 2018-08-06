package com.camerakit.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20.*
import android.opengl.Matrix
import com.camerakit.R
import com.camerakit.type.CameraFacing
import com.camerakit.util.RawResReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraSurfaceTexture(val inputTexture: Int, context: Context, var width: Int, var height: Int) : SurfaceTexture(inputTexture) {

    var facing: CameraFacing = CameraFacing.BACK
        set(value) {
            field = value
            vertexBuffer.position(0)
            if (value == CameraFacing.BACK) {
                vertexBuffer.put(IDENTITY_VERTEX_DATA, 0, IDENTITY_VERTEX_DATA.size)
            } else if (value == CameraFacing.FRONT) {
                vertexBuffer.put(IDENTITY_VERTEX_DATA_FRONT, 0, IDENTITY_VERTEX_DATA_FRONT.size)
            }
        }

    var orientation: Int = 0
        set(degrees) {
            field = degrees
            Matrix.setIdentityM(rotationMatrix, 0)
            Matrix.rotateM(rotationMatrix, 0, degrees.toFloat(), 0f, 0f, 1f)
        }

    private var bufferWidth: Int = 0
    private var bufferHeight: Int = 0

    private val previewProgram: PreviewProgram
    private val vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(IDENTITY_VERTEX_DATA.size * BYTES_PER_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(IDENTITY_VERTEX_DATA)

    private val rotationMatrix: FloatArray = FloatArray(16)
    private val scaleMatrix: FloatArray = FloatArray(16)

    init {
        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.setIdentityM(scaleMatrix, 0)

        val vertexShaderCode = RawResReader(context, R.raw.camera_vsh).value
        val previewFragmentShaderCode = RawResReader(context, R.raw.camera_fsh).value
        previewProgram = PreviewProgram(vertexShaderCode, previewFragmentShaderCode)
        previewProgram.link()
    }

    override fun setDefaultBufferSize(bufferWidth: Int, bufferHeight: Int) {
        super.setDefaultBufferSize(bufferWidth, bufferHeight)
        this.bufferWidth = bufferWidth
        this.bufferHeight = bufferHeight

        updateScaleMatrix(bufferWidth, bufferHeight)
    }

    fun updateScaleMatrix(bufferWidth: Int, bufferHeight: Int) {
        val previewWidth: Int
        val previewHeight: Int
        if (orientation % 180 == 0) {
            previewWidth = bufferWidth
            previewHeight = bufferHeight
        } else {
            previewWidth = bufferHeight
            previewHeight = bufferWidth
        }

        val ratioSurface = width.toFloat() / height.toFloat()
        val ratioPreview = previewWidth.toFloat() / previewHeight.toFloat()

        val scaleX: Float
        val scaleY: Float
        if (ratioSurface > ratioPreview) {
            scaleX = 1f
            scaleY = (width.toFloat() / previewWidth.toFloat()) * (previewHeight.toFloat() / height.toFloat())
        } else {
            scaleX = (height.toFloat() / previewHeight.toFloat()) * (previewWidth.toFloat() / width.toFloat())
            scaleY = 1f
        }

        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.scaleM(scaleMatrix, 0, scaleX, scaleY, 1f)
    }

    fun setSurfaceSize(width: Int, height: Int) {
        this.width = width
        this.height = height

        if (bufferWidth != 0 && bufferHeight != 0) {
            updateScaleMatrix(bufferWidth, bufferHeight)
        }
    }

    fun draw() {
        glClearColor(0f, 0f, 0f, 1f)
        glViewport(0, 0, width, height)

        previewProgram.use()

        val textures = intArrayOf(inputTexture)
        val buffers = intArrayOf(0)

        for (i in 0..0) {
            glBindFramebuffer(GL_FRAMEBUFFER, buffers[i])
            previewProgram.setUniforms(textures[i], rotationMatrix, scaleMatrix)

            vertexBuffer.position(0)
            glVertexAttribPointer(previewProgram.aPositionLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
            glEnableVertexAttribArray(previewProgram.aPositionLocation)

            vertexBuffer.position(2)
            glVertexAttribPointer(previewProgram.aTextureCoordinatesLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
            glEnableVertexAttribArray(previewProgram.aTextureCoordinatesLocation)

            glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexBuffer.capacity() / 4)
        }
    }

    companion object {
        private val BYTES_PER_FLOAT = 4
        private val IDENTITY_VERTEX_DATA = floatArrayOf(
                -1.0f, +1.0f, 0.0f, 0.0f,
                +1.0f, +1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f, 1.0f,
                +1.0f, -1.0f, 1.0f, 1.0f
        )

        private val IDENTITY_VERTEX_DATA_FRONT = floatArrayOf(
                -1.0f, -1.0f, 0.0f, 0.0f,
                +1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f, +1.0f, 0.0f, 1.0f,
                +1.0f, +1.0f, 1.0f, 1.0f
        )
    }

}