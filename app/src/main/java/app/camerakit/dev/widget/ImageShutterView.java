package app.camerakit.dev.widget;

import android.content.Context;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import app.camerakit.dev.R;

public class ImageShutterView extends AppCompatImageView {

    private AnimatedVectorDrawableCompat downAnimDrawable;
    private AnimatedVectorDrawableCompat upAnimDrawable;

    public ImageShutterView(Context context) {
        super(context);
        initialize();
    }

    public ImageShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setImageResource(R.drawable.ic_shutter);
        downAnimDrawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_shutter_down);
        upAnimDrawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_shutter_up);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                setImageDrawable(downAnimDrawable);
                downAnimDrawable.start();
                break;
            }

            case MotionEvent.ACTION_UP: {
                performClick();
                setImageDrawable(upAnimDrawable);
                upAnimDrawable.start();
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
