package com.flurgle.camerakit.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends AppCompatActivity implements View.OnLayoutChangeListener {

    @BindView(R.id.activity_main)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    @BindView(R.id.focusMarker)
    FocusMarkerLayout focusMarker;

    // Capture Mode:

    @BindView(R.id.captureModeRadioGroup)
    RadioGroup captureModeRadioGroup;

    // Crop Mode:

    @BindView(R.id.cropModeRadioGroup)
    RadioGroup cropModeRadioGroup;

    // Width:

    @BindView(R.id.screenWidth)
    TextView screenWidth;
    @BindView(R.id.width)
    EditText width;
    @BindView(R.id.widthUpdate)
    Button widthUpdate;
    @BindView(R.id.widthModeRadioGroup)
    RadioGroup widthModeRadioGroup;

    // Height:

    @BindView(R.id.screenHeight)
    TextView screenHeight;
    @BindView(R.id.height)
    EditText height;
    @BindView(R.id.heightUpdate)
    Button heightUpdate;
    @BindView(R.id.heightModeRadioGroup)
    RadioGroup heightModeRadioGroup;

    private int mCameraWidth;
    private int mCameraHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth.setText("screen: " + parent.getWidth() + "px");
                screenHeight.setText("screen: " + parent.getHeight() + "px");
            }
        });

        camera.addOnLayoutChangeListener(this);

        captureModeRadioGroup.setOnCheckedChangeListener(captureModeChangedListener);
        cropModeRadioGroup.setOnCheckedChangeListener(cropModeChangedListener);
        widthModeRadioGroup.setOnCheckedChangeListener(widthModeChangedListener);
        heightModeRadioGroup.setOnCheckedChangeListener(heightModeChangedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    protected void onPause() {
        camera.stop();
        super.onPause();
    }

    @OnClick(R.id.capturePhoto)
    void capturePhoto() {
        final long startTime = System.currentTimeMillis();
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                long callbackTime = System.currentTimeMillis();
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                ResultHolder.dispose();
                ResultHolder.setCameraRotation(camera.getCameraRotation());
                ResultHolder.setImage(bitmap);
                ResultHolder.setNativeCaptureSize(
                        captureModeRadioGroup.getCheckedRadioButtonId() == R.id.modeCaptureStandard ?
                                camera.getCaptureSize() : camera.getPreviewSize()
                );
                ResultHolder.setTimeToCallback(callbackTime - startTime);
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivity(intent);
            }
        });
        camera.captureImage();
    }

    @OnClick(R.id.captureVideo)
    void captureVideo() {
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
            }
        });

        camera.startRecordingVideo();
        camera.postDelayed(new Runnable() {
            @Override
            public void run() {
                camera.stopRecordingVideo();
            }
        }, 3000);
    }

    @OnClick(R.id.toggleCamera)
    void toggleCamera() {
        switch (camera.toggleFacing()) {
            case CameraKit.Constants.FACING_BACK:
                Toast.makeText(this, "Switched to back camera!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FACING_FRONT:
                Toast.makeText(this, "Switched to front camera!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.toggleFlash)
    void toggleFlash() {
        switch (camera.toggleFlash()) {
            case CameraKit.Constants.FLASH_ON:
                Toast.makeText(this, "Flash on!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FLASH_OFF:
                Toast.makeText(this, "Flash off!", Toast.LENGTH_SHORT).show();
                break;

            case CameraKit.Constants.FLASH_AUTO:
                Toast.makeText(this, "Flash auto!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    RadioGroup.OnCheckedChangeListener captureModeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            camera.setMethod(
                    checkedId == R.id.modeCaptureStandard ?
                            CameraKit.Constants.METHOD_STANDARD :
                            CameraKit.Constants.METHOD_STILL
            );

            Toast.makeText(MainActivity.this, "Picture capture set to" + (checkedId == R.id.modeCaptureStandard ? " quality!" : " speed!"), Toast.LENGTH_SHORT).show();
        }
    };

    RadioGroup.OnCheckedChangeListener cropModeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            camera.setCropOutput(
                    checkedId == R.id.modeCropVisible
            );

            Toast.makeText(MainActivity.this, "Picture cropping is" + (checkedId == R.id.modeCropVisible ? " on!" : " off!"), Toast.LENGTH_SHORT).show();
        }
    };

    @OnClick(R.id.widthUpdate)
    void widthUpdateClicked() {
        if (widthUpdate.getAlpha() >= 1) {
            updateCamera(true, false);
        }
    }

    RadioGroup.OnCheckedChangeListener widthModeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            widthUpdate.setEnabled(checkedId == R.id.widthCustom);
            widthUpdate.setAlpha(checkedId == R.id.widthCustom ? 1f : 0.3f);
            width.clearFocus();
            width.setEnabled(checkedId == R.id.widthCustom);
            width.setAlpha(checkedId == R.id.widthCustom ? 1f : 0.5f);

            updateCamera(true, false);
        }
    };

    @OnClick(R.id.heightUpdate)
    void heightUpdateClicked() {
        if (heightUpdate.getAlpha() >= 1) {
            updateCamera(false, true);
        }
    }

    RadioGroup.OnCheckedChangeListener heightModeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            heightUpdate.setEnabled(checkedId == R.id.heightCustom);
            heightUpdate.setAlpha(checkedId == R.id.heightCustom ? 1f : 0.3f);
            height.clearFocus();
            height.setEnabled(checkedId == R.id.heightCustom);
            height.setAlpha(checkedId == R.id.heightCustom ? 1f : 0.5f);

            updateCamera(false, true);
        }
    };

    private void updateCamera(boolean updateWidth, boolean updateHeight) {
        ViewGroup.LayoutParams cameraLayoutParams = camera.getLayoutParams();
        int width = cameraLayoutParams.width;
        int height = cameraLayoutParams.height;

        if (updateWidth) {
            switch (widthModeRadioGroup.getCheckedRadioButtonId()) {
                case R.id.widthCustom:
                    String widthInput = this.width.getText().toString();
                    if (widthInput.length() > 0) {
                        try {
                            width = Integer.valueOf(widthInput);
                        } catch (Exception e) {

                        }
                    }

                    break;

                case R.id.widthWrapContent:
                    width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    break;

                case R.id.widthMatchParent:
                    width = ViewGroup.LayoutParams.MATCH_PARENT;
                    break;
            }
        }

        if (updateHeight) {
            switch (heightModeRadioGroup.getCheckedRadioButtonId()) {
                case R.id.heightCustom:
                    String heightInput = this.height.getText().toString();
                    if (heightInput.length() > 0) {
                        try {
                            height = Integer.valueOf(heightInput);
                        } catch (Exception e) {

                        }
                    }
                    break;

                case R.id.heightWrapContent:
                    height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    break;

                case R.id.heightMatchParent:
                    height = parent.getHeight();
                    break;
            }
        }

        cameraLayoutParams.width = width;
        cameraLayoutParams.height = height;

        camera.addOnLayoutChangeListener(this);
        camera.setLayoutParams(cameraLayoutParams);

        Toast.makeText(this, (updateWidth && updateHeight ? "Width and height" : updateWidth ? "Width" : "Height") + " updated!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        mCameraWidth = right - left;
        mCameraHeight = bottom - top;

        width.setText(String.valueOf(mCameraWidth));
        height.setText(String.valueOf(mCameraHeight));

        camera.removeOnLayoutChangeListener(this);
    }

    @OnTouch(R.id.focusMarker)
    boolean onTouchCamera(View view, MotionEvent motionEvent) {
        focusMarker.focus(motionEvent.getX(), motionEvent.getY());
        return false;
    }

}
