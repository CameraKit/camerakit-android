package com.wonderkiln.camerakit.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

public class CameraControls extends LinearLayout {

    private int cameraViewId = -1;
    private CameraView cameraView;

    private int coverViewId = -1;
    private View coverView;

    ImageView facingButton;
    ImageView flashButton;
    ImageView captureImageButton;
    ImageView captureVideoButton;

    private long captureStartTime;
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

        facingButton = findViewById(R.id.facingButton);
        flashButton = findViewById(R.id.flashButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        captureVideoButton = findViewById(R.id.captureVideoButton);

        facingButton.setOnTouchListener(onTouchFacing);
        flashButton.setOnTouchListener(onTouchFlash);
        captureImageButton.setOnTouchListener(onTouchCaptureImage);
        captureVideoButton.setOnTouchListener(onTouchCaptureVideo);

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
                cameraView.bindCameraKitListener(this);
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

    public void imageCaptured(CameraKitImage image) {
        byte[] jpeg = image.getJpeg();

        long callbackTime = System.currentTimeMillis();
        ResultHolder.dispose();
        ResultHolder.setImage(jpeg);
        ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
        ResultHolder.setTimeToCallback(callbackTime - captureStartTime);
        Intent intent = new Intent(getContext(), PreviewActivity.class);
        getContext().startActivity(intent);
    }

    public void videoCaptured(CameraKitVideo video) {
        File videoFile = video.getVideoFile();
        if (videoFile != null) {
            ResultHolder.dispose();
            ResultHolder.setVideo(videoFile);
            ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
            Intent intent = new Intent(getContext(), PreviewActivity.class);
            getContext().startActivity(intent);
        }
    }

    private View.OnTouchListener onTouchCaptureImage = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    handleViewTouchFeedback(view, motionEvent);
                    if (capturingVideo) {
                        capturingVideo = false;
                        cameraView.stopVideo();
                    } else {
                        captureStartTime = System.currentTimeMillis();
                        cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                            @Override
                            public void callback(CameraKitImage image) {
                                imageCaptured(image);
                            }
                        });
                    }
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    handleViewTouchFeedback(view, motionEvent);
                    break;
                }
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchCaptureVideo = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    pendingVideoCapture = true;
                    handleViewTouchFeedback(view, motionEvent);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (pendingVideoCapture) {
                                capturingVideo = true;
                                cameraView.captureVideo(new CameraKitEventCallback<CameraKitVideo>() {
                                    @Override
                                    public void callback(CameraKitVideo video) {
                                        videoCaptured(video);
                                    }
                                });
                            }
                        }
                    }, 250);
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    pendingVideoCapture = false;
                    if (capturingVideo) {
                        capturingVideo = false;
                        cameraView.stopVideo();
                    }
                    handleViewTouchFeedback(view, motionEvent);
                }
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchFacing = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
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
    };

    private View.OnTouchListener onTouchFlash = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_UP: {
                    if (cameraView.getFlash() == CameraKit.Constants.FLASH_OFF) {
                        cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                        changeViewImageResource((ImageView) view, R.drawable.ic_flash_on);
                    } else {
                        cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                        changeViewImageResource((ImageView) view, R.drawable.ic_flash_off);
                    }
                    break;
                }
            }
            return true;
        }
    };

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
