package camerakit.android

import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.FrameLayout
import camerakit.android.camera.Camera
import camerakit.android.camera.CameraException
import camerakit.android.camera.core.CameraAttributes
import camerakit.android.preview.CameraSurfaceTexture
import camerakit.android.preview.CameraSurfaceTextureListener
import camerakit.android.preview.CameraSurfaceView
import camerakit.android.util.CameraSize
import camerakit.android.util.CameraSizeCalculator
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.runBlocking

class CameraPreview : FrameLayout {

    private var state: CameraState = CameraState.RELEASED
    private var displayOrientation: Int = 0

    private var cameraSurfaceTexture: CameraSurfaceTexture? = null
    private var cameraAttributes: CameraAttributes? = null

    private val cameraSurfaceView: CameraSurfaceView = CameraSurfaceView(context)
    private val cameraDispatcher: CoroutineDispatcher = newSingleThreadContext("CAMERA")

    private val camera: Camera = Camera(context)

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attributeSet: AttributeSet) :
            super(context, attributeSet)

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        displayOrientation = windowManager.defaultDisplay.rotation * 90

        cameraSurfaceView.cameraSurfaceTextureListener = object : CameraSurfaceTextureListener {
            override fun onSurfaceReady(cameraSurfaceTexture: CameraSurfaceTexture) {
                this@CameraPreview.cameraSurfaceTexture = cameraSurfaceTexture
                if (state == CameraState.STARTED) {
                    start()
                }
            }
        }

        addView(cameraSurfaceView)
    }

    fun open() {
        launch(cameraDispatcher) {
            runBlocking {
                try {
                    cameraAttributes = camera.startCamera()
                } catch (e: CameraException) {
                }

                state = CameraState.OPENED
            }
        }
    }

    fun start() {
        launch(cameraDispatcher) {
            runBlocking {
                val cameraSurfaceTexture = cameraSurfaceTexture
                val cameraAttributes = cameraAttributes
                if (cameraSurfaceTexture != null && cameraAttributes != null) {
                    val previewOrientation = (cameraAttributes.sensorOrientation - displayOrientation + 360) % 360
                    val previewSize = CameraSizeCalculator(cameraAttributes.previewSizes)
                            .findClosestSizeContainingTarget(when (previewOrientation % 180 == 0) {
                                true -> CameraSize(width, height)
                                false -> CameraSize(height, width)
                            })

                    try {
                        camera.startPreview(cameraSurfaceTexture, previewSize, previewOrientation)
                    } catch (e: CameraException) {
                    }
                }

                state = CameraState.STARTED
            }
        }
    }

    fun stop() {
        launch(cameraDispatcher) {
            runBlocking {
                try {
                    camera.stopPreview()
                } catch (e: CameraException) {
                }

                state = CameraState.STOPPED
            }
        }
    }

    fun release() {
        launch(cameraDispatcher) {
            runBlocking {
                try {
                    camera.stopCamera()
                } catch (e: CameraException) {
                }

                state = CameraState.RELEASED
            }
        }
    }

    private enum class CameraState {
        OPENED,
        STARTED,
        STOPPED,
        RELEASED;
    }

}