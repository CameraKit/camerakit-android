package camerakit.android

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CameraKitView : FrameLayout {

    private var viewState: ViewState = ViewState.STOPPED
    private val cameraPreview: CameraPreview = CameraPreview(context)

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attributeSet: AttributeSet) :
            super(context, attributeSet)

    init {
        addView(cameraPreview)
    }

    fun onStart() {
        cameraPreview.open()
        viewState = ViewState.STARTED
    }

    fun onResume() {
        cameraPreview.start()
        viewState = ViewState.RESUMED
    }

    fun onPause() {
        cameraPreview.stop()
        viewState = ViewState.PAUSED
    }

    fun onStop() {
        cameraPreview.release()
        viewState = ViewState.STOPPED
    }

    private enum class ViewState {
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED;
    }

}