package com.camerakit.surface

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

internal class DrawModel(vertices: FloatArray, texCoords: FloatArray, val coordsPerVertex: Int) {

    val vertexCount = vertices.size / coordsPerVertex
    val vertexBuffer = toFloatBuffer(vertices)
    val vertexStride = coordsPerVertex * FLOAT_SIZE

    val texCoordBuffer = toFloatBuffer(texCoords)
    val texCoordStride = coordsPerVertex * FLOAT_SIZE

    companion object {

        const val FLOAT_SIZE = 4

        const val FULL_RECT_COORDS_PER_VERTEX = 2

        val FULL_RECT_VERTICES = floatArrayOf(
                -1.0f, -1.0f,
                +1.0f, -1.0f,
                -1.0f, +1.0f,
                +1.0f, +1.0f
        )

        val FULL_RECT_TEX_COORDS = floatArrayOf(
                +0.0f, +0.0f,
                +1.0f, +0.0f,
                +0.0f, +1.0f,
                +1.0f, +1.0f
        )

        private fun toFloatBuffer(floatArray: FloatArray): FloatBuffer {
            val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4)
            byteBuffer.order(ByteOrder.nativeOrder())
            val floatBuffer = byteBuffer.asFloatBuffer()
            floatBuffer.put(floatArray)
            floatBuffer.position(0)
            return floatBuffer
        }

    }

}
