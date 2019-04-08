package app.camerakit.dev.widget;

import android.content.Context;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import app.camerakit.dev.R;

public class VideoShutterView extends AppCompatImageView {

    private boolean isRecording = false;

    private AnimatedVectorDrawableCompat startAnimDrawable;
    private AnimatedVectorDrawableCompat stopAnimDrawable;

    public VideoShutterView(Context context) {
        super(context);
        initialize();
    }

    public VideoShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setImageResource(R.drawable.ic_shutter_video);
        startAnimDrawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_shutter_video_start);
        stopAnimDrawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_shutter_video_stop);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }

            case MotionEvent.ACTION_UP: {
                performClick();

                if (!isRecording) {
                    setImageDrawable(startAnimDrawable);
                    startAnimDrawable.start();
                } else {
                    setImageDrawable(stopAnimDrawable);
                    stopAnimDrawable.start();
                }

                isRecording = !isRecording;
                break;
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
