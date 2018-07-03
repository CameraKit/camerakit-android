package com.camerakit.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CameraSurfaceView : GLSurfaceView {

    internal val renderer: CameraSurfaceRenderer

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setEGLContextClientVersion(2)

        renderer = CameraSurfaceRenderer(context)
        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
        keepScreenOn = true
    }

    fun setShader(shader: Int) {
        renderer.shader = shader
    }

    fun setOrientation(displayRotation: Int, sensorOrientation: Int, facing: Int) {
        renderer.setOrientation(displayRotation, sensorOrientation, facing)
    }

    fun awaitSurface(callback: Callback) {
        launch(UI) {
            val surfaceTexture = renderer.getSurfaceTexture()
            callback.run(surfaceTexture)
        }
    }

    interface Callback {
        fun run(surfaceTexture: SurfaceTexture)
    }

}