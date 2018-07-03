package com.camerakit.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.camerakit.CameraKit
import kotlinx.coroutines.experimental.delay
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class CameraSurfaceRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var textureId: Int = -1
    private var surfaceTexture: SurfaceTexture? = null

    private var textureProgram: TextureProgram? = null

    var shader: Int = 0
        set(value) {
            field = value
            textureProgram?.let {
                it.release()

                var type = TextureProgram.Type.TEXTURE_EXT
                if (value == 1) {
                    type = TextureProgram.Type.TEXTURE_EDGES
                } else if (value == 2) {
                    type = TextureProgram.Type.TEXTURE_TWIRL
                } else if (value == 3) {
                    type = TextureProgram.Type.TEXTURE_WARP
                }

                textureProgram = TextureProgram(context, type)
            }
        }

    private val drawModel = DrawModel(
            vertices = DrawModel.FULL_RECT_VERTICES,
            texCoords = DrawModel.FULL_RECT_TEX_COORDS,
            coordsPerVertex = DrawModel.FULL_RECT_COORDS_PER_VERTEX
    )

    private val drawModelProjector = DrawModelProjector(drawModel)

    suspend fun getSurfaceTexture(): SurfaceTexture {
        while (true) {
            if (surfaceTexture != null) {
                return surfaceTexture!!
            }

            delay(50)
        }
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        var type = TextureProgram.Type.TEXTURE_EXT
        if (shader == 1) {
            type = TextureProgram.Type.TEXTURE_EDGES
        } else if (shader == 2) {
            type = TextureProgram.Type.TEXTURE_TWIRL
        } else if (shader == 3) {
            type = TextureProgram.Type.TEXTURE_WARP
        }

        val textureProgram = TextureProgram(context, type)
        textureId = textureProgram.genTexture()
        surfaceTexture = SurfaceTexture(textureId)
        drawModelProjector.textureId = textureId

        this.textureProgram = textureProgram
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        surfaceTexture?.run {
            updateTexImage()
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            gl.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            textureProgram?.let {
                drawModelProjector.draw(it)
            }
        }
    }

    fun setOrientation(displayRotation: Int, sensorOrientation: Int, facing: Int) {
        if (facing == CameraKit.FACING_FRONT) {
            drawModelProjector.setScale(-1f, -1f)
            drawModelProjector.setRotation((sensorOrientation + displayRotation).toFloat())
        } else {
            drawModelProjector.setScale(-1f, 1f)
            drawModelProjector.setRotation((sensorOrientation + displayRotation).toFloat())
        }
    }


}