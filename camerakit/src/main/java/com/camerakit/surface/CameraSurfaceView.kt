package com.camerakit.surface

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.camerakit.surface.opengl.GLUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraSurfaceView : GLSurfaceView, GLSurfaceView.Renderer {

    var texture: Int = 0
    private var cameraTexture: CameraSurfaceTexture? = null
    private var surfaceTextureCallback: ((surfaceTexture: CameraSurfaceTexture) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun awaitSurfaceTexture(callback: (surfaceTexture: CameraSurfaceTexture) -> Unit) {
        val texture = cameraTexture
        if (texture != null) {
            callback(texture)
        } else {
            surfaceTextureCallback = callback
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        cameraTexture = null
        surfaceTextureCallback = null
        super.onPause()
    }

    // GLSurfaceView.Renderer:

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLUtil.genTexture(this::texture)
        val surfaceTexture = CameraSurfaceTexture(texture, context, width, height)

        val callback = surfaceTextureCallback
        if (callback != null) {
            callback(surfaceTexture)
        }

        surfaceTexture.setOnFrameAvailableListener {
            requestRender()
        }

        cameraTexture = surfaceTexture
    }

    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        cameraTexture?.updateTexImage()

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        cameraTexture?.draw()
    }

}