package com.camerakit.surface

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PreviewVertexData {
    private val vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(IDENTITY.size * BYTES_PER_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(IDENTITY)

    var capacity: Int = vertexBuffer.capacity()

//    fun reconfigure(facing: CameraFacing, orientation: Int, viewportSize: CameraSize, previewSize: CameraSize) {
//        vertexBuffer.position(0)
//        vertexBuffer.put(IDENTITY)
//
//        if (facing == CameraFacing.BACK) {
//            for (i in 0..15 step 4) {
////                vertexBuffer.put(i, IDENTITY[i] * -1.0f)
//            }
//        }
//
//        val viewportWidth = viewportSize.width.toFloat()
//        val viewportHeight = viewportSize.height.toFloat()
//
//        val previewWidth: Float
//        val previewHeight: Float
//        if (orientation % 180 == 0) {
//            previewWidth = previewSize.width.toFloat()
//            previewHeight = previewSize.height.toFloat()
//        } else {
//            previewWidth = previewSize.height.toFloat()
//            previewHeight = previewSize.width.toFloat()
//        }
//
//        val scaleX = (previewWidth / previewHeight) / (viewportWidth / viewportHeight)
//        if (scaleX > 1f) {
//            val scaledWidth = scaleX * viewportWidth
//            val offsetX = (scaledWidth - viewportWidth) / scaledWidth / 2
//
//            for (i in 2..15 step 4) {
////                vertexBuffer.put(i,  Math.abs(IDENTITY[i] - offsetX))
//            }
//        }
//
//        val scaleY = (previewHeight / previewWidth) / (viewportHeight / viewportWidth)
//        if (scaleY > 1f) {
//            val scaledHeight = scaleY * viewportHeight
//            val offsetY = (scaledHeight - viewportHeight) / scaledHeight / 2
//
//            for (i in 3..15 step 4) {
////                vertexBuffer.put(i,  Math.abs(IDENTITY[i] - offsetY))
//            }
//        }
//
//    }

    fun setVertexAttributePointers(aPositionLocation: Int, aTextureCoordinatesLocation: Int) {
        vertexBuffer.position(0)
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
        glEnableVertexAttribArray(aPositionLocation)

        vertexBuffer.position(2)
        glVertexAttribPointer(aTextureCoordinatesLocation, 2, GL_FLOAT, false, 16, vertexBuffer)
        glEnableVertexAttribArray(aTextureCoordinatesLocation)

        vertexBuffer.position(0)
    }

    companion object {
        private const val BYTES_PER_FLOAT = 4
        private val IDENTITY = floatArrayOf(
                -1.0f, -1.0f, 0.0f, 0.0f,
                +1.0f, -1.0f, 1.0f, 0.0f,
                -1.0f, +1.0f, 0.0f, 1.0f,
                +1.0f, +1.0f, 1.0f, 1.0f
        )

    }

}
