package com.camerakit.preview

import android.content.Context
import android.opengl.GLES20.glGenTextures
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.annotation.Keep
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraSurfaceView : GLSurfaceView, GLSurfaceView.Renderer {

    var cameraSurfaceTextureListener: CameraSurfaceTextureListener? = null
    private var cameraSurfaceTexture: CameraSurfaceTexture? = null

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attributeSet: AttributeSet)
            : super(context, attributeSet)

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY

        nativeInit()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    // GLSurfaceView.Renderer:

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        genTextures { inputTexture, outputTexture ->
            cameraSurfaceTexture = CameraSurfaceTexture(inputTexture, outputTexture).apply {
                setOnFrameAvailableListener { requestRender() }
                cameraSurfaceTextureListener?.onSurfaceReady(this)
            }
        }

        nativeOnSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        nativeOnSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        val cameraSurfaceTexture = cameraSurfaceTexture
        if (cameraSurfaceTexture != null) {
            nativeOnDrawFrame()

            cameraSurfaceTexture.updateTexImage()
            nativeDrawTexture(cameraSurfaceTexture.outputTexture,
                    cameraSurfaceTexture.size.width,
                    cameraSurfaceTexture.size.height)
        }
    }

    // Other:

    private fun genTextures(textureCallback: (inputTexture: Int, outputTexture: Int) -> Unit) {
        val textures = IntArray(2)
        glGenTextures(2, textures, 0)
        textureCallback(textures[0], textures[1])
    }

    // ---

    @Keep
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

    private external fun nativeInit()

    private external fun nativeOnSurfaceCreated()

    private external fun nativeOnSurfaceChanged(width: Int, height: Int)

    private external fun nativeOnDrawFrame()

    private external fun nativeDrawTexture(texture: Int, textureWidth: Int, textureHeight: Int)

    private external fun nativeFinalize()

    private external fun nativeRelease()

    companion object {

        init {
            if (Build.VERSION.SDK_INT <= 17) {
                System.loadLibrary("camerakit-core")
            }
            System.loadLibrary("camerakit")
        }

    }

}
