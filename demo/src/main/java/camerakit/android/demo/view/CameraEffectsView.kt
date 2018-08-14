package camerakit.android.demo.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import camerakit.android.demo.R
import camerakit.android.demo.ext.dpToPx

class CameraEffectsView : FrameLayout {

    private val photoEffect = PhotoEffect(context)
    private val videoEffect = VideoEffect(context)
    private val snapshotEffect = SnapshotEffect(context)
    private val recordingEffect = RecordingEffect(context)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        addView(videoEffect)
        addView(snapshotEffect)
        addView(photoEffect)
        addView(recordingEffect)
    }

    fun onNativePhotoCapture() {
        photoEffect.effectEnabled = true
    }

    fun onCameraKitPhotoCapture() {
        snapshotEffect.effectEnabled = true
    }

    fun onNativeVideoCapture(recording: Boolean) {
        recordingEffect.effectEnabled = recording
    }

    fun onCameraKitVideoCapture(recording: Boolean) {
        videoEffect.effectEnabled = recording
    }

    private class SnapshotEffect(context: Context) : View(context) {

        var effectEnabled = false
            set(enable) {
                field = enable
                if (enable) {
                    alpha = 0.66f
                    animate().alpha(0f)
                            .setStartDelay(100)
                            .setDuration(600)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    effectEnabled = false
                                }
                            })
                            .start()
                } else {
                    alpha = 0f
                }
            }

        private val paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.color = Color.WHITE

            alpha = 0f
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            val thickness = 5.dpToPx()
            canvas.drawRect(Rect(0, 0, thickness, height), paint)
            canvas.drawRect(Rect(0, 0, width, thickness), paint)
            canvas.drawRect(Rect(width - thickness, 0, width, height), paint)
            canvas.drawRect(Rect(0, height - thickness, width, height), paint)
        }

    }

    private class VideoEffect(context: Context) : View(context) {

        var effectEnabled = false
            set(enable) {
                field = enable
                if (enable) {
                    alpha = 0.9f
                    startAnimation(effectAnimation)
                } else {
                    clearAnimation()
                    alpha = 0f
                }
            }

        private val paint = Paint()
        private val effectAnimation: Animation

        init {
            paint.isAntiAlias = true
            paint.color = ContextCompat.getColor(context, R.color.video_red)

            effectAnimation = AlphaAnimation(0.2f, 0.9f)
            effectAnimation.duration = 600
            effectAnimation.repeatMode = AlphaAnimation.REVERSE
            effectAnimation.repeatCount = AlphaAnimation.INFINITE

            alpha = 0f
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            val thickness = 5.dpToPx()
            canvas.drawRect(Rect(0, 0, thickness, height), paint)
            canvas.drawRect(Rect(0, 0, width, thickness), paint)
            canvas.drawRect(Rect(width - thickness, 0, width, height), paint)
            canvas.drawRect(Rect(0, height - thickness, width, height), paint)
        }

    }

    private class PhotoEffect(context: Context) : View(context) {

        var effectEnabled = false
            set(enable) {
                field = enable
                if (enable) {
                    alpha = 0.66f
                    animate().alpha(0f)
                            .setStartDelay(100)
                            .setDuration(400)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    effectEnabled = false
                                }
                            })
                            .start()
                } else {
                    alpha = 0f
                }
            }


        private val paint = Paint()

        init {
            paint.isAntiAlias = true
            paint.color = Color.WHITE

            alpha = 0f
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            canvas.drawRect(Rect(0, 0, width, height), paint)
        }

    }

    class RecordingEffect(context: Context) : View(context) {

        var effectEnabled = false
            set(enable) {
                field = enable
                if (enable) {
                    alpha = 1f
                    startAnimation(effectAnimation)
                } else {
                    clearAnimation()
                    alpha = 0f
                }
            }


        private val paint = Paint()
        private val effectAnimation: Animation

        init {
            paint.isAntiAlias = true
            paint.color = ContextCompat.getColor(context, R.color.video_red)

            effectAnimation = AlphaAnimation(0f, 0.2f)
            effectAnimation.duration = 600
            effectAnimation.repeatMode = AlphaAnimation.REVERSE
            effectAnimation.repeatCount = AlphaAnimation.INFINITE

            alpha = 0f
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            canvas.drawRect(Rect(0, 0, width, height), paint)
        }

    }

}