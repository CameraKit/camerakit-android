package com.camerakit.surface.opengl

import android.opengl.GLES20.*
import android.opengl.Matrix
import kotlin.reflect.KMutableProperty0

abstract class GLProgram {

    private val uMatrixModelLocation: Int by lazy {
        uniformLocation("u_Model")
    }

    private val uMatrixViewPosition: Int by lazy {
        uniformLocation("u_View")
    }

    private val uMatrixProjectionPosition: Int by lazy {
        uniformLocation("u_Projection")
    }

    val aPositionLocation: Int by lazy {
        attributeLocation("a_Position")
    }

    val aTextureCoordinatesLocation: Int by lazy {
        attributeLocation("a_TextureCoordinates")
    }

    var infoLog: String = ""

    var program: Int = 0
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0

    constructor(vertexShaderCode: String, fragmentShaderCode: String) {
        program = glCreateProgram()
        if (program == 0) {
            throw GLProgramException()
        }

        if (!compileShader(this::vertexShader, GL_VERTEX_SHADER, vertexShaderCode)) {
            throw GLShaderException()
        }

        if (!compileShader(this::fragmentShader, GL_FRAGMENT_SHADER, fragmentShaderCode)) {
            throw GLShaderException()
        }

        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
    }


    fun link(): Boolean {
        glLinkProgram(program)

        val linkStatus = IntArray(1)
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] != GL_TRUE) {
            return false
        }

        if (vertexShader != 0) {
            glDetachShader(program, vertexShader)
            glDeleteShader(vertexShader)
            vertexShader = 0
        }

        if (fragmentShader != 0) {
            glDetachShader(program, fragmentShader)
            glDeleteShader(fragmentShader)
            fragmentShader = 0
        }

        return true
    }

    fun validate() {
        glValidateProgram(program)

        val logLength = IntArray(1)
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, logLength, 0)

        if (logLength[0] > 0) {
            infoLog = glGetProgramInfoLog(program)
        }
    }

    fun use() {
        glUseProgram(program)
    }

    fun delete() {
        glUseProgram(0)
        glDeleteProgram(program)
    }

    fun uniformLocation(uniformName: String): Int {
        return glGetUniformLocation(program, uniformName)
    }

    fun attributeLocation(attributeName: String): Int {
        return glGetAttribLocation(program, attributeName)
    }

    open fun setUniforms(inputTexture: Int,
                         modelMatrix: FloatArray = IDENTITY_MATRIX,
                         viewMatrix: FloatArray = IDENTITY_MATRIX,
                         projectionMatrix: FloatArray = IDENTITY_MATRIX) {

        glUniformMatrix4fv(uMatrixModelLocation, 1, false, modelMatrix, 0)
        glUniformMatrix4fv(uMatrixViewPosition, 1, false, viewMatrix, 0)
        glUniformMatrix4fv(uMatrixProjectionPosition, 1, false, projectionMatrix, 0)
    }

    companion object {
        protected val IDENTITY_MATRIX = FloatArray(16)

        init {
            Matrix.setIdentityM(IDENTITY_MATRIX, 0)
        }

        private fun compileShader(dstProperty: KMutableProperty0<Int>, type: Int, shaderCode: String): Boolean {
            val shader = glCreateShader(type)

            glShaderSource(shader, shaderCode)
            glCompileShader(shader)

            val compileStatus = IntArray(1)
            glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0)

            if (compileStatus[0] != GL_TRUE) {
                return false
            }

            dstProperty.set(shader)
            return true
        }
    }

}