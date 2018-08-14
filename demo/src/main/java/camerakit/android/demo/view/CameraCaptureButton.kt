package camerakit.android.demo.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import camerakit.android.demo.R

class CameraCaptureButton : FrameLayout {

    var actionListener: ActionListener? = null

    private val photoCapturing: View
    private val videoCapturing: View

    private var isCapturingVideo: Boolean = false

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attributeSet: AttributeSet) :
            super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.camera_capture_button, this, true)
        photoCapturing = findViewById(R.id.photoCapturing)
        videoCapturing = findViewById(R.id.videoCapturing)

        setOnClickListener {
            if (isCapturingVideo) {
                isCapturingVideo = false
                stopVideoCapture()
            } else {
                photoCapture()
            }
        }

        setOnLongClickListener {
            if (isCapturingVideo) {
                false
            } else {
                isCapturingVideo = true
                startVideoCapture()
                true
            }
        }
    }

    fun photoCapture() {
        actionListener?.onPhotoCapture()
    }

    fun startVideoCapture() {
        videoCapturing.visibility = View.INVISIBLE
        if (Build.VERSION.SDK_INT >= 21) {
            val cx = width / 2
            val cy = height / 2
            val radius = Math.hypot(cx.toDouble(), cy.toDouble())
            val reveal = ViewAnimationUtils.createCircularReveal(videoCapturing, cx, cy, 0f, radius.toFloat())
            videoCapturing.visibility = View.VISIBLE
            reveal.start()
        } else {
            videoCapturing.visibility = View.VISIBLE
        }

        actionListener?.onStartVideoCapture()
    }

    fun stopVideoCapture() {
        if (Build.VERSION.SDK_INT >= 21) {
            val cx = videoCapturing.width / 2
            val cy = videoCapturing.height / 2
            val radius = Math.hypot(cx.toDouble(), cy.toDouble())
            val hide = ViewAnimationUtils.createCircularReveal(videoCapturing, cx, cy, radius.toFloat(), 0f)
            hide.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    videoCapturing.visibility = View.INVISIBLE
                }
            })
            hide.start()
        } else {
            videoCapturing.visibility = View.INVISIBLE
        }

        actionListener?.onStopVideoCapture()
    }

    interface ActionListener {

        fun onPhotoCapture()
        fun onStartVideoCapture()
        fun onStopVideoCapture()

    }

}