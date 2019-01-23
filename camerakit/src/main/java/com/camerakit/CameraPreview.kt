package com.camerakit

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.WindowManager
import android.widget.FrameLayout
import com.camerakit.preview.CameraSurfaceTexture
import com.camerakit.preview.CameraSurfaceTextureListener
import com.camerakit.preview.CameraSurfaceView
import com.camerakit.util.CameraSizeCalculator
import com.camerakit.api.CameraApi
import com.camerakit.api.CameraAttributes
import com.camerakit.api.CameraEvents
import com.camerakit.api.ManagedCameraApi
import com.camerakit.api.camera1.Camera1
import com.camerakit.api.camera2.Camera2
import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize
import com.jpegkit.Jpeg
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraPreview : FrameLayout, CameraEvents {

    companion object {
        private const val FORCE_DEPRECATED_API = false
    }

    var lifecycleState: LifecycleState = LifecycleState.STOPPED
    var surfaceState: SurfaceState = SurfaceState.SURFACE_WAITING
    var cameraState: CameraState = CameraState.CAMERA_CLOSED
        set(state) {
            field = state
            when (state) {
                CameraState.CAMERA_OPENED -> {
                    listener?.onCameraOpened()
                }
                CameraState.PREVIEW_STARTED -> {
                    listener?.onPreviewStarted()
                }
                CameraState.PREVIEW_STOPPED -> {
                    listener?.onPreviewStopped()
                }
                CameraState.CAMERA_CLOSING -> {
                    listener?.onCameraClosed()
                }
                else -> {
                    // ignore
                }
            }
        }

    var listener: Listener? = null

    var displayOrientation: Int = 0
    var previewOrientation: Int = 0
    var captureOrientation: Int = 0
    var previewSize: CameraSize = CameraSize(0, 0)
    var surfaceSize: CameraSize = CameraSize(0, 0)
        get() {
            return surfaceTexture?.size ?: field
        }

    var photoSize: CameraSize = CameraSize(0, 0)
    var flash: CameraFlash = CameraFlash.OFF
    var imageMegaPixels: Float = 2f

    private var cameraFacing: CameraFacing = CameraFacing.BACK
    private var surfaceTexture: CameraSurfaceTexture? = null
    private var attributes: CameraAttributes? = null

    private val cameraSurfaceView: CameraSurfaceView = CameraSurfaceView(context)

    private val cameraDispatcher: CoroutineDispatcher = newSingleThreadContext("CAMERA")
    private var cameraOpenContinuation: Continuation<Unit>? = null
    private var previewStartContinuation: Continuation<Unit>? = null

    @SuppressWarnings("NewApi")
    private val cameraApi: CameraApi = ManagedCameraApi(
            when (Build.VERSION.SDK_INT < 21 || FORCE_DEPRECATED_API) {
                true -> Camera1(this)
                false -> Camera2(this, context)
            })

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attributeSet: AttributeSet) :
            super(context, attributeSet)

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        displayOrientation = windowManager.defaultDisplay.rotation * 90

        cameraSurfaceView.cameraSurfaceTextureListener = object : CameraSurfaceTextureListener {
            override fun onSurfaceReady(cameraSurfaceTexture: CameraSurfaceTexture) {
                surfaceTexture = cameraSurfaceTexture
                surfaceState = SurfaceState.SURFACE_AVAILABLE
                if (lifecycleState == LifecycleState.STARTED || lifecycleState == LifecycleState.RESUMED) {
                    resume()
                }
            }
        }

        addView(cameraSurfaceView)
    }

    fun start(facing: CameraFacing) {
        GlobalScope.launch(cameraDispatcher) {
            runBlocking {
                lifecycleState = LifecycleState.STARTED
                cameraFacing = facing
                openCamera()
            }
        }
    }

    fun resume() {
        GlobalScope.launch(cameraDispatcher) {
            runBlocking {
                lifecycleState = LifecycleState.RESUMED
                try {
                    startPreview()
                } catch (e: Exception) {
                    // camera or surface not ready, wait.
                }
            }
        }
    }

    fun pause() {
        GlobalScope.launch(cameraDispatcher) {
            runBlocking {
                lifecycleState = LifecycleState.PAUSED
                stopPreview()
            }
        }
    }

    fun stop() {
        GlobalScope.launch(cameraDispatcher) {
            runBlocking {
                lifecycleState = LifecycleState.STOPPED
                closeCamera()
            }
        }
    }

    fun capturePhoto(callback: PhotoCallback) {
        GlobalScope.launch(cameraDispatcher) {
            runBlocking {
                cameraApi.setFlash(flash)
                cameraApi.capturePhoto {
                    cameraApi.cameraHandler.post {
                        val jpeg = Jpeg(it)
                        jpeg.rotate(captureOrientation)
                        val transformedBytes = jpeg.jpegBytes
                        jpeg.release()
                        callback.onCapture(transformedBytes)
                    }
                }
            }
        }
    }

    fun hasFlash(): Boolean {
        if (attributes?.flashes != null) {
            return true
        }
        return false
    }

    fun getSupportedFlashTypes(): Array<CameraFlash>? {
        return attributes?.flashes
    }

    interface PhotoCallback {
        fun onCapture(jpeg: ByteArray)
    }

    // CameraEvents:

    override fun onCameraOpened(cameraAttributes: CameraAttributes) {
        cameraState = CameraState.CAMERA_OPENED
        attributes = cameraAttributes
        cameraOpenContinuation?.resume(Unit)
        cameraOpenContinuation = null
    }

    override fun onCameraClosed() {
        cameraState = CameraState.CAMERA_CLOSED
    }

    override fun onCameraError() {
    }

    override fun onPreviewStarted() {
        cameraState = CameraState.PREVIEW_STARTED
        previewStartContinuation?.resume(Unit)
        previewStartContinuation = null
    }

    override fun onPreviewStopped() {
        cameraState = CameraState.PREVIEW_STOPPED
    }

    override fun onPreviewError() {
    }

    // State enums:

    enum class LifecycleState {
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED;
    }

    enum class SurfaceState {
        SURFACE_AVAILABLE,
        SURFACE_WAITING;
    }

    enum class CameraState {
        CAMERA_OPENING,
        CAMERA_OPENED,
        PREVIEW_STARTING,
        PREVIEW_STARTED,
        PREVIEW_STOPPING,
        PREVIEW_STOPPED,
        CAMERA_CLOSING,
        CAMERA_CLOSED;
    }

    // Camera control:

    private suspend fun openCamera(): Unit = suspendCoroutine {
        cameraOpenContinuation = it
        cameraState = CameraState.CAMERA_OPENING
        cameraApi.open(cameraFacing)
    }

    private suspend fun startPreview(): Unit = suspendCoroutine {
        previewStartContinuation = it
        val surfaceTexture = surfaceTexture
        val attributes = attributes
        if (surfaceTexture != null && attributes != null) {
            cameraState = CameraState.PREVIEW_STARTING

            previewOrientation = when (cameraFacing) {
                CameraFacing.BACK -> (attributes.sensorOrientation - displayOrientation + 360) % 360
                CameraFacing.FRONT -> {
                    val result = (attributes.sensorOrientation + displayOrientation) % 360
                    (360 - result) % 360
                }
            }

            captureOrientation = when (cameraFacing) {
                CameraFacing.BACK -> (attributes.sensorOrientation - displayOrientation + 360) % 360
                CameraFacing.FRONT -> (attributes.sensorOrientation + displayOrientation + 360) % 360
            }

            if (Build.VERSION.SDK_INT >= 21 && !FORCE_DEPRECATED_API) {
                surfaceTexture.setRotation(displayOrientation)
            }

            previewSize = CameraSizeCalculator(attributes.previewSizes)
                    .findClosestSizeContainingTarget(when (previewOrientation % 180 == 0) {
                        true -> CameraSize(width, height)
                        false -> CameraSize(height, width)
                    })

            surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
            surfaceTexture.size = when (previewOrientation % 180) {
                0 -> previewSize
                else -> CameraSize(previewSize.height, previewSize.width)
            }

            photoSize = CameraSizeCalculator(attributes.photoSizes)
                    .findClosestSizeMatchingArea((imageMegaPixels * 1000000).toInt())

            cameraApi.setPreviewOrientation(previewOrientation)
            cameraApi.setPreviewSize(previewSize)
            cameraApi.setPhotoSize(photoSize)
            cameraApi.startPreview(surfaceTexture)
        } else {
            it.resumeWithException(IllegalStateException())
            previewStartContinuation = null
        }
    }

    private suspend fun stopPreview(): Unit = suspendCoroutine {
        cameraState = CameraState.PREVIEW_STOPPING
        cameraApi.stopPreview()
        it.resume(Unit)
    }

    private suspend fun closeCamera(): Unit = suspendCoroutine {
        cameraState = CameraState.CAMERA_CLOSING
        cameraApi.release()
        it.resume(Unit)
    }

    // Listener:

    interface Listener {
        fun onCameraOpened()
        fun onCameraClosed()
        fun onPreviewStarted()
        fun onPreviewStopped()
    }

}