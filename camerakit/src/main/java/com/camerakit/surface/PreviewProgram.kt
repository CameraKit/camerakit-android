package com.camerakit.surface

import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import com.camerakit.surface.opengl.GLProgram

class PreviewProgram(vertexShaderCode: String, fragmentShaderCode: String) :
        GLProgram(vertexShaderCode, fragmentShaderCode) {

    private val uSampleExternalOESLocation: Int by lazy {
        uniformLocation("u_SamplerExternalOES")
    }

    override fun setUniforms(inputTexture: Int, modelMatrix: FloatArray, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        super.setUniforms(inputTexture, modelMatrix, viewMatrix, projectionMatrix)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTexture)
        glUniform1i(uSampleExternalOESLocation, 0)
    }

}