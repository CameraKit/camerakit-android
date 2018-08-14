package camerakit.android.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import camerakit.android.CameraKitView
import camerakit.android.demo.view.CameraCaptureButton
import camerakit.android.demo.view.CameraEffectsView

class CameraFragment : Fragment(), CameraCaptureButton.ActionListener {

    private lateinit var camera: CameraKitView

    private lateinit var cameraEffectsView: CameraEffectsView
    private lateinit var cameraCaptureButton: CameraCaptureButton

    private lateinit var camerakitPhotoBtn: FloatingActionButton
    private lateinit var camerakitVideoBtn: FloatingActionButton

    private var recordingCameraKitVideo = false
        set(capturing) {
            field = capturing
            if (capturing) {
                startCameraKitVideoCapture()
            } else {
                stopCameraKitVideoCapture()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_camera, container, false)

        camera = rootView.findViewById(R.id.camera)

        cameraEffectsView = rootView.findViewById(R.id.cameraEffectsView)

        cameraCaptureButton = rootView.findViewById(R.id.cameraCaptureButton)
        cameraCaptureButton.actionListener = this

        camerakitPhotoBtn = rootView.findViewById(R.id.camerakitPhotoBtn)
        camerakitPhotoBtn.setOnClickListener {
            captureCameraKitPhoto()
        }

        camerakitVideoBtn = rootView.findViewById(R.id.camerakitVideoBtn)
        camerakitVideoBtn.setOnClickListener {
            recordingCameraKitVideo = !recordingCameraKitVideo
            if (recordingCameraKitVideo) {
                camerakitVideoBtn.setImageResource(R.drawable.ic_stop)
                startCameraKitVideoCapture()
            } else {
                camerakitVideoBtn.setImageResource(R.drawable.ic_video)
                stopCameraKitVideoCapture()
            }
        }


        val facingOption = rootView.findViewById<View>(R.id.facingOption)
        val facingIcons = intArrayOf(R.drawable.ic_facing_front, R.drawable.ic_facing_back)
        var facingIconIndex = 0
        facingOption.setOnClickListener {
            transitionOptionView(it.findViewWithTag("optionImageView"), facingIcons[++facingIconIndex % facingIcons.size])
        }

        val flashOption = rootView.findViewById<View>(R.id.flashOption)
        val flashIcons = intArrayOf(R.drawable.ic_flash_off, R.drawable.ic_flash_on, R.drawable.ic_flash_auto)
        var flashIconIndex = 0
        flashOption.setOnClickListener {
            transitionOptionView(it.findViewWithTag("optionImageView"), flashIcons[++flashIconIndex % flashIcons.size])
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        camera.onStart()
    }

    override fun onResume() {
        super.onResume()
        camera.onResume()
    }

    override fun onPause() {
        super.onPause()
        camera.onPause()
    }

    override fun onStop() {
        super.onStop()
        camera.onStop()
    }

    private fun transitionOptionView(imageView: ImageView, @DrawableRes nextDrawableResId: Int) {
        imageView.animate()
                .translationY(imageView.height.toFloat())
                .setDuration(80)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        imageView.setImageResource(nextDrawableResId)
                        imageView.translationY *= -1f
                        imageView.animate()
                                .translationY(0f)
                                .setDuration(80)
                                .setListener(null)
                                .start()
                    }
                })
                .start()
    }

    fun captureCameraKitPhoto() {
        cameraEffectsView.onCameraKitPhotoCapture()
    }

    fun startCameraKitVideoCapture() {
        cameraEffectsView.onCameraKitVideoCapture(true)
    }

    fun stopCameraKitVideoCapture() {
        cameraEffectsView.onCameraKitVideoCapture(false)
    }

    override fun onPhotoCapture() {
        cameraEffectsView.onNativePhotoCapture()
    }

    override fun onStartVideoCapture() {
        cameraEffectsView.onNativeVideoCapture(true)
    }

    override fun onStopVideoCapture() {
        cameraEffectsView.onNativeVideoCapture(false)
    }

}