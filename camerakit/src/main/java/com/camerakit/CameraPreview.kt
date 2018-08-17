package com.camerakit

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager
import android.widget.FrameLayout
import com.camerakit.api.CameraApi
import com.camerakit.api.CameraAttributes
import com.camerakit.api.CameraEvents
import com.camerakit.api.ManagedCameraApi
import com.camerakit.api.camera1.Camera1
import com.camerakit.api.camera2.Camera2
import com.camerakit.surface.CameraSurfaceTexture
import com.camerakit.surface.CameraSurfaceView
import com.camerakit.type.CameraFacing
import com.camerakit.type.CameraFlash
import com.camerakit.type.CameraSize
import com.jpegkit.Jpeg
import java.util.*
import kotlin.math.absoluteValue

class CameraPreview : FrameLayout, CameraEvents {

    companion object {
        private const val FORCE_DEPRECATED_API = false
    }

    var listener: Listener? = null

    var flash: CameraFlash = CameraFlash.OFF
        set(value) {
            field = value
            cameraApi.setFlash(value)
        }

    private var displayOrientation: Int = 0

    private val cameraApi: CameraApi

    private val surfaceView: CameraSurfaceView

    private var cameraSurfaceTexture: CameraSurfaceTexture? = null
    private var captureOrientation = 0

    private var previewWidth: Int = 0
    private var previewHeight: Int = 0
    private var previewOrientation: Int = 0

    private var photoWidth: Int = 0
    private var photoHeight: Int = 0
    private var imageMegaPixels: Float = 2f

    private var isCameraOpen: Boolean = false
    private var isPreviewStarted: Boolean = false
    private var attachedToWindow: Boolean = false

    private var startWaiting: Boolean = false
    private var facing: CameraFacing = CameraFacing.BACK
    private var resumeWaiting: Boolean = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        surfaceView = CameraSurfaceView(context)
        addView(surfaceView)

        @SuppressWarnings("NewApi")
        cameraApi = ManagedCameraApi(
                when (Build.VERSION.SDK_INT < 21 || FORCE_DEPRECATED_API) {
                    true -> Camera1(this)
                    false -> Camera2(this, context)
                })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        displayOrientation = windowManager.defaultDisplay.rotation * 90

        if ((width > 0 && height > 0) || Build.VERSION.SDK_INT < 19) {
            attachedToWindow = true
            if (startWaiting) {
                start(facing)
            }
        }
    }

    override fun onDetachedFromWindow() {
        attachedToWindow = false
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (Build.VERSION.SDK_INT >= 19 && !attachedToWindow && isAttachedToWindow && w > 0 && h > 0) {
            attachedToWindow = true
            if (startWaiting) {
                start(facing)
            }
        }
    }

    fun start(facing: CameraFacing) {
        this.facing = facing

        if (attachedToWindow) {
            startWaiting = false
            isCameraOpen = true
            cameraApi.open(facing)
        } else {
            startWaiting = true
        }
    }

    fun resume() {
        isPreviewStarted = true
        if (attachedToWindow) {
            resumeWaiting = false
            val cameraSurfaceTexture = cameraSurfaceTexture
            if (cameraSurfaceTexture != null && isCameraOpen) {
                cameraApi.startPreview(cameraSurfaceTexture)
            }
        } else {
            resumeWaiting = true
        }
    }

    fun pause() {
        resumeWaiting = false
        isPreviewStarted = false
        if (attachedToWindow) {
            cameraApi.stopPreview()
        }
    }

    fun stop() {
        cameraSurfaceTexture = null

        startWaiting = false
        resumeWaiting = false
        isCameraOpen = false

        surfaceView.onPause()
        surfaceView.stop()
        cameraApi.release()
    }

    fun setFacing(facing: CameraFacing) {
        this.facing = facing

        if (attachedToWindow) {
            surfaceView.onPause()

            cameraApi.stopPreview()
            cameraApi.release()
            cameraApi.open(facing)
            resume()
        }
    }

    fun setImageMegaPixels(megaPixels: Float, photoSizes: Array<CameraSize>? = null) {
        imageMegaPixels = megaPixels
        if (photoSizes != null) {
            if (photoSizes.isNotEmpty()) {
                var photoSize: CameraSize = photoSizes.first()

                val targetPixelCount = megaPixels * 1000000
                photoSizes.forEach {
                    if ((targetPixelCount - it.width * it.height).absoluteValue
                            < (targetPixelCount - photoSize.width * photoSize.height).absoluteValue) {
                        photoSize = it
                    }
                }

                photoWidth = photoSize.width
                photoHeight = photoSize.height
                cameraApi.setPhotoSize(photoSize)
            }
        }
    }

    fun capturePhoto(callback: PhotoCallback) {
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

    interface PhotoCallback {
        fun onCapture(jpeg: ByteArray)
    }

    fun getPreviewSize(): CameraSize? {
        if (previewWidth == 0 || previewHeight == 0) {
            return null
        }

        return CameraSize(previewWidth, previewHeight)
    }

    fun getAdjustedPreviewSize(): CameraSize? {
        if (previewWidth == 0 || previewHeight == 0) {
            return null
        }

        if (previewOrientation % 180 == 0) {
            return CameraSize(previewWidth, previewHeight)
        } else {
            return CameraSize(previewHeight, previewWidth)
        }
    }

    fun getPhotoSize(): CameraSize? {
        if (photoWidth == 0 || photoHeight == 0) {
            return null
        }

        return CameraSize(photoWidth, photoHeight)
    }

    interface Listener {
        fun onCameraOpened()
        fun onCameraClosed()
        fun onPreviewStarted()
        fun onPreviewStopped()
    }

    // CameraEvents:

    override fun onCameraOpened(cameraAttributes: CameraAttributes) {
        captureOrientation = when (facing) {
            CameraFacing.BACK -> (cameraAttributes.sensorOrientation - displayOrientation + 360) % 360
            CameraFacing.FRONT -> (cameraAttributes.sensorOrientation + displayOrientation + 360) % 360
        }

        previewOrientation = (displayOrientation - cameraAttributes.sensorOrientation + 360) % 360

        val previewSize = when (previewOrientation % 180) {
            0 -> bestPreviewSize(cameraAttributes.previewSizes, width, height)
            else -> bestPreviewSize(cameraAttributes.previewSizes, height, width)
        }

        previewWidth = previewSize.width
        previewHeight = previewSize.height

        cameraApi.setPreviewSize(previewSize)
        setImageMegaPixels(imageMegaPixels, cameraAttributes.photoSizes)

        listener?.onCameraOpened()

        surfaceView.onResume()
        surfaceView.awaitSurfaceTexture { surfaceTexture ->
            cameraSurfaceTexture = surfaceTexture
            surfaceTexture.facing = cameraAttributes.facing
            surfaceTexture.orientation = previewOrientation
            surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

            if (isPreviewStarted) {
                resume()
            }

            surfaceTexture.setOnFrameAvailableListener {
                surfaceView.requestRender()
            }
        }
    }

    private fun bestPreviewSize(choices: Array<CameraSize>, viewWidth: Int, viewHeight: Int): CameraSize {
        val larger: ArrayList<CameraSize> = ArrayList()
        val smaller: ArrayList<CameraSize> = ArrayList()

        choices.forEach {
            if (it.width >= viewWidth && it.height >= viewHeight) {
                larger += it
            } else {
                smaller += it
            }
        }

        if (larger.size > 0) {
            return Collections.min(larger)
        }

        if (smaller.size > 0) {
            return Collections.max(smaller)
        }

        return CameraSize(0, 0)
    }

    override fun onCameraClosed() {
        listener?.onCameraClosed()
    }

    override fun onCameraError() {
    }

    override fun onPreviewStarted() {
        listener?.onPreviewStarted()
    }

    override fun onPreviewStopped() {
        listener?.onPreviewStopped()
    }

    override fun onPreviewError() {
    }

    // Gestures:

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return super.onDown(e)
        }

    })

    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val dspan = detector.currentSpan - detector.previousSpan
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
        override fun onScaleEnd(detector: ScaleGestureDetector) {}

    })

}
