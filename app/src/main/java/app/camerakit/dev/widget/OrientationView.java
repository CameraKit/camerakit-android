package app.camerakit.dev.widget;

import android.content.Context;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import app.camerakit.dev.R;

public class OrientationView extends AppCompatImageView {

    private AnimatedVectorDrawableCompat animDrawable;

    public OrientationView(Context context) {
        super(context);
        initialize();
    }

    public OrientationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        animDrawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_orientation);
        setImageDrawable(animDrawable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }

            case MotionEvent.ACTION_UP: {
                animDrawable.start();
                performClick();
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
