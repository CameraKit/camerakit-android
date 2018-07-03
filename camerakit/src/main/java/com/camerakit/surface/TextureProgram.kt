package com.camerakit.surface

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import com.camerakit.R
import java.nio.FloatBuffer

internal class TextureProgram(context: Context, type: Type) {

    internal enum class Type(val target: Int) {

        TEXTURE_2D(GLES20.GL_TEXTURE_2D) {
            override fun vertexShader(context: Context): String {
                return ShaderResource(context, R.raw.vertex_shader).toString()
            }

            override fun fragmentShader(context: Context): String {
                return ShaderResource(context, R.raw.fragment_shader_2d).toString()
            }
        },

        TEXTURE_EXT(GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            override fun vertexShader(context: Context): String {
                return ShaderResource(context, R.raw.vertex_shader).toString()
            }

            override fun fragmentShader(context: Context): String {
                return ShaderResource(context, R.raw.fragment_shader_ext).toString()
            }
        },

        TEXTURE_EDGES(GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            override fun vertexShader(context: Context): String {
                return ShaderResource(context, R.raw.vertex_shader).toString()
            }

            override fun fragmentShader(context: Context): String {
                return ShaderResource(context, R.raw.fragment_shader_ext_edges).toString()
            }
        },

        TEXTURE_TWIRL(GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            override fun vertexShader(context: Context): String {
                return ShaderResource(context, R.raw.vertex_shader).toString()
            }

            override fun fragmentShader(context: Context): String {
                return ShaderResource(context, R.raw.fragment_shader_ext_twirl).toString()
            }
        },

        TEXTURE_WARP(GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            override fun vertexShader(context: Context): String {
                return ShaderResource(context, R.raw.vertex_shader).toString()
            }

            override fun fragmentShader(context: Context): String {
                return ShaderResource(context, R.raw.fragment_shader_ext_warp).toString()
            }
        };

        abstract fun vertexShader(context: Context): String
        abstract fun fragmentShader(context: Context): String

    }

    private var target: Int = type.target
    private var program: Int = createGlProgram(type.vertexShader(context), type.fragmentShader(context))

    private var aPositionLocation: Int = 0
    private var aTextureCoordLocation: Int = 0
    private var uMVPMatrixLocation: Int = 0
    private var uTexMatrixLocation: Int = 0

    init {
        if (program == 0) {
            throw RuntimeException("Error creating program.")
        }

        aPositionLocation = glGetAttribLocation(program, "aPosition")
        checkGlLocation(aPositionLocation, "aPosition")

        aTextureCoordLocation = glGetAttribLocation(program, "aTextureCoord")
        checkGlLocation(aTextureCoordLocation, "aTextureCoord")

        uMVPMatrixLocation = glGetUniformLocation(program, "uMVPMatrix")
        checkGlLocation(uMVPMatrixLocation, "uMVPMatrix")

        uTexMatrixLocation = glGetUniformLocation(program, "uTexMatrix")
        checkGlLocation(uTexMatrixLocation, "uTexMatrix")
    }

    fun release() {
        glDeleteProgram(program)
        program = -1
    }

    fun genTexture(): Int {
        val textures = intArrayOf(0)
        glGenTextures(1, textures, 0)
        checkGlError("glGenTextures")

        val textureId = textures[0]
        glBindTexture(target, textureId)
        checkGlError("glBindTexture")

        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        checkGlError("glTexParameter")

        return textureId
    }

    fun draw(mvpMatrix: FloatArray, vertexBuffer: FloatBuffer, firstVertex: Int, vertexCount: Int, coordsPerVertex: Int,
             vertexStride: Int, texMatrix: FloatArray, texBuffer: FloatBuffer, textureId: Int, texStride: Int) {
        glUseProgram(program)
        checkGlError("glUseProgram")

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(target, textureId)

        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)
        checkGlError("glUniformMatrix4fv")

        glUniformMatrix4fv(uTexMatrixLocation, 1, false, texMatrix, 0)
        checkGlError("glUniformMatrix4fv")

        glEnableVertexAttribArray(aPositionLocation)
        checkGlError("glEnableVertexAttribArray")

        glVertexAttribPointer(aPositionLocation, coordsPerVertex, GL_FLOAT, false, vertexStride, vertexBuffer)
        checkGlError("glVertexAttribPointer")

        glEnableVertexAttribArray(aTextureCoordLocation)
        checkGlError("glEnableVertexAttribArray")

        glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, texStride, texBuffer)
        checkGlError("glVertexAttribPointer")

        glDrawArrays(GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        checkGlError("glDrawArrays")

        glDisableVertexAttribArray(aPositionLocation)
        glDisableVertexAttribArray(aTextureCoordLocation)
        glBindTexture(target, 0)
        glUseProgram(0)
    }

    companion object {

        private fun createGlProgram(vertexSource: String, fragmentSource: String): Int {
            val vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource)
            if (vertexShader == 0) {
                return 0
            }

            val pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource)
            if (pixelShader == 0) {
                return 0
            }

            var program = glCreateProgram()
            checkGlError("glCreateProgram")
            if (program == 0) {
                // error
            }

            glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")

            glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")

            glLinkProgram(program)

            val linkStatus = intArrayOf(0)
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)

            if (linkStatus[0] != GL_TRUE) {
                glDeleteProgram(program)
                program = 0
            }

            return program
        }

        private fun loadShader(shaderType: Int, source: String): Int {
            var shader = glCreateShader(shaderType)
            checkGlError("glCreateShader($shaderType)")
            glShaderSource(shader, source)
            glCompileShader(shader)
            val compiled = IntArray(1)
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)

            if (compiled[0] == 0) {
                glDeleteShader(shader)
                shader = 0
            }

            return shader
        }

        private fun checkGlError(call: String) {
            val error = glGetError()
            if (error != GL_NO_ERROR) {
                val message = "$call: glError 0x${Integer.toHexString(error)}";
                throw RuntimeException(message)
            }
        }

        private fun checkGlLocation(location: Int, name: String) {
            if (location < 0) {
                throw RuntimeException("Error locating $name in program.")
            }
        }

    }

}