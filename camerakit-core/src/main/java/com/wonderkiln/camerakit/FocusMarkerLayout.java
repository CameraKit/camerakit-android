package com.wonderkiln.camerakit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wonderkiln.camerakit.core.R;

public class FocusMarkerLayout extends FrameLayout {

    private FrameLayout mFocusMarkerContainer;
    private ImageView mFill;

    public FocusMarkerLayout(@NonNull Context context) {
        this(context, null);
    }

    public FocusMarkerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_focus_marker, this);

        mFocusMarkerContainer = findViewById(R.id.focusMarkerContainer);
        mFill = findViewById(R.id.fill);

        mFocusMarkerContainer.setAlpha(0);
    }

    public void focus(float mx, float my) {
        mx *= getWidth();
        my *= getHeight();
        int x = (int) (mx - mFocusMarkerContainer.getWidth() / 2);
        int y = (int) (my - mFocusMarkerContainer.getWidth() / 2);

        mFocusMarkerContainer.setTranslationX(x);
        mFocusMarkerContainer.setTranslationY(y);

        mFocusMarkerContainer.animate().setListener(null).cancel();
        mFill.animate().setListener(null).cancel();

        mFill.setScaleX(0);
        mFill.setScaleY(0);
        mFill.setAlpha(1f);

        mFocusMarkerContainer.setScaleX(1.36f);
        mFocusMarkerContainer.setScaleY(1.36f);
        mFocusMarkerContainer.setAlpha(1f);

        mFocusMarkerContainer.animate().scaleX(1).scaleY(1).setStartDelay(0).setDuration(330)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFocusMarkerContainer.animate().alpha(0).setStartDelay(750).setDuration(800).setListener(null).start();
                    }
                }).start();

        mFill.animate().scaleX(1).scaleY(1).setDuration(330)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFill.animate().alpha(0).setDuration(800).setListener(null).start();
                    }
                }).start();

    }


}
