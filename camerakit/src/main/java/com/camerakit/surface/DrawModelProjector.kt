package com.camerakit.surface

import android.opengl.Matrix

internal class DrawModelProjector(private val drawModel: DrawModel) {

    var textureId: Int = -1

    private var matrixReady: Boolean = false

    private val modelViewMatrix = FloatArray(16)
        get() {
            if (!matrixReady) {
                recomputeMatrix(field)
            }

            return field
        }

    private val scratchMatrix = FloatArray(16)

    private var translateX: Float = 0.0f
    private var translateY: Float = 0.0f

    private var angle: Float = 0.0f

    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f

    init {
    }

    fun recomputeMatrix(matrix: FloatArray) {
        Matrix.setIdentityM(matrix, 0)
        Matrix.translateM(matrix, 0, translateX, translateY, 0.0f)

        if (angle != 0.0f) {
            Matrix.rotateM(matrix, 0, angle, 0.0f, 0.0f, 1.0f)
        }

        Matrix.scaleM(matrix, 0, scaleX, scaleY, 1.0f)

        matrixReady = true
    }

    fun setTranslation(tx: Float = translateX, ty: Float = translateY) {
        translateX = tx
        translateY = ty

        matrixReady = false
    }

    fun setRotation(degrees: Float = angle) {
        angle = degrees % 360

        matrixReady = false
    }

    fun setScale(sx: Float = scaleX, sy: Float = scaleY) {
        scaleX = sx
        scaleY = sy

        matrixReady = false
    }

    fun draw(program: TextureProgram) {
        program.draw(
                mvpMatrix = modelViewMatrix,
                vertexBuffer = drawModel.vertexBuffer,
                firstVertex = 0,
                vertexCount = drawModel.vertexCount,
                coordsPerVertex = drawModel.coordsPerVertex,
                vertexStride = drawModel.vertexStride,
                texMatrix = IDENTITY_MATRIX,
                texBuffer = drawModel.texCoordBuffer,
                textureId = textureId,
                texStride = drawModel.texCoordStride
        )
    }

    companion object {
        private val IDENTITY_MATRIX = FloatArray(16)

        init {
            Matrix.setIdentityM(IDENTITY_MATRIX, 0)
        }
    }

}