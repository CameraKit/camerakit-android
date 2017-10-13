package com.wonderkiln.camerakit.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

public class CameraControls extends LinearLayout {

    private int cameraViewId = -1;
    private CameraView cameraView;

    private int coverViewId = -1;
    private View coverView;

    @BindView(R.id.facingButton)
    ImageView facingButton;

    @BindView(R.id.flashButton)
    ImageView flashButton;

    private long captureDownTime;
    private boolean pendingVideoCapture;
    private boolean capturingVideo;

    public CameraControls(Context context) {
        this(context, null);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0);

            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1);
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cameraViewId != -1) {
            View view = getRootView().findViewById(cameraViewId);
            if (view instanceof CameraView) {
                cameraView = (CameraView) view;
                setFacingImageBasedOnCamera();
            }
        }

        if (coverViewId != -1) {
            View view = getRootView().findViewById(coverViewId);
            if (view != null) {
                coverView = view;
                coverView.setVisibility(GONE);
            }
        }
    }

    private void setFacingImageBasedOnCamera() {
        if (cameraView.isFacingFront()) {
            facingButton.setImageResource(R.drawable.ic_facing_back);
        } else {
            facingButton.setImageResource(R.drawable.ic_facing_front);
        }
    }

    @OnTouch(R.id.captureButton)
    boolean onTouchCapture(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                captureDownTime = System.currentTimeMillis();
//                pendingVideoCapture = true;
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (pendingVideoCapture) {
//                            capturingVideo = true;
//                            cameraView.startRecordingVideo();
//                        }
//                    }
//                }, 250);
                break;
            }

            case MotionEvent.ACTION_UP: {
                pendingVideoCapture = false;

                if (capturingVideo) {
                    capturingVideo = false;
                    cameraView.setCameraListener(new CameraListener() {
                        @Override
                        public void onVideoTaken(File video) {
                            super.onVideoTaken(video);
                            if (video != null) {
                                ResultHolder.dispose();
                                ResultHolder.setVideo(video);
                                ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
                                Intent intent = new Intent(getContext(), PreviewActivity.class);
                                getContext().startActivity(intent);
                            }
                        }
                    });
                    cameraView.stopRecordingVideo();
                } else {
                    final long startTime = System.currentTimeMillis();
                    cameraView.setCameraListener(new CameraListener() {
                        @Override
                        public void onPictureTaken(byte[] jpeg) {
                            super.onPictureTaken(jpeg);
                            long callbackTime = System.currentTimeMillis();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                            ResultHolder.dispose();
                            ResultHolder.setImage(bitmap);
                            ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
                            ResultHolder.setTimeToCallback(callbackTime - startTime);
                            Intent intent = new Intent(getContext(), PreviewActivity.class);
                            getContext().startActivity(intent);
                        }
                    });
                    cameraView.captureImage();
                }
                break;
            }
        }
        return true;
    }

    @OnTouch(R.id.facingButton)
    boolean onTouchFacing(final View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                coverView.setAlpha(0);
                coverView.setVisibility(VISIBLE);
                coverView.animate()
                        .alpha(1)
                        .setStartDelay(0)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (cameraView.isFacingFront()) {
                                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_front);
                                } else {
                                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                                    changeViewImageResource((ImageView) view, R.drawable.ic_facing_back);
                                }

                                coverView.animate()
                                        .alpha(0)
                                        .setStartDelay(200)
                                        .setDuration(300)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                coverView.setVisibility(GONE);
                                            }
                                        })
                                        .start();
                            }
                        })
                        .start();

                break;
            }
        }
        return true;
    }

    @OnTouch(R.id.flashButton)
    boolean onTouchFlash(View view, MotionEvent motionEvent) {
        handleViewTouchFeedback(view, motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP: {
                if (true) {
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_off);
                } else {
                    changeViewImageResource((ImageView) view, R.drawable.ic_flash_on);
                }

                break;
            }
        }
        return true;
    }

    boolean handleViewTouchFeedback(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownAnimation(view);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                touchUpAnimation(view);
                return true;
            }

            default: {
                return true;
            }
        }
    }

    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.setRotation(0);
        imageView.animate()
                .rotationBy(360)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(resId);
            }
        }, 120);
    }

}
