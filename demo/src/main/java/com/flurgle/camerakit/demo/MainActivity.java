package com.flurgle.camerakit.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.activity_main)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    // Capture Mode:

    @BindView(R.id.captureModeRadioGroup)
    RadioGroup captureModeRadioGroup;

    // Crop Mode:

    @BindView(R.id.cropModeRadioGroup)
    RadioGroup cropModeRadioGroup;

    // Tap to Focus:

    @BindView(R.id.tapToFocusModeRadioGroup)
    RadioGroup tapToFocusModeRadioGroup;

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

        camera.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCameraWidth = right - left;
                mCameraHeight = bottom - top;

                width.setText(String.valueOf(mCameraWidth));
                height.setText(String.valueOf(mCameraHeight));
            }
        });

        captureModeRadioGroup.setOnCheckedChangeListener(captureModeChangedListener);
        cropModeRadioGroup.setOnCheckedChangeListener(cropModeChangedListener);
        tapToFocusModeRadioGroup.setOnCheckedChangeListener(tapToFocusModeChangedListener);
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
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                MediaHolder.dispose();
                MediaHolder.setImage(bitmap);
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivity(intent);
            }
        });
        camera.capturePicture();
    }

    @OnClick(R.id.captureVideo)
    void captureVideo() {
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                MediaHolder.dispose();
                MediaHolder.setVideo(video);
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivity(intent);
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
            camera.setPictureMode(
                    checkedId == R.id.modeCaptureQuality ?
                            CameraKit.Constants.PICTURE_MODE_QUALITY :
                            CameraKit.Constants.PICTURE_MODE_SPEED
            );

            Toast.makeText(MainActivity.this, "Picture capture set to" + (checkedId == R.id.modeCaptureQuality ? " quality!" : " speed!"), Toast.LENGTH_SHORT).show();
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

    RadioGroup.OnCheckedChangeListener tapToFocusModeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            camera.setTapToFocus(
                    checkedId == R.id.modeTapToFocusVisible ?
                            CameraKit.Constants.TAP_TO_FOCUS_VISIBLE :
                            checkedId == R.id.modeTapToFocusInvisible ?
                                    CameraKit.Constants.TAP_TO_FOCUS_INVISIBLE :
                                    CameraKit.Constants.TAP_TO_FOCUS_OFF
            );

            Toast.makeText(MainActivity.this, "Tap to focus is" + (checkedId == R.id.modeTapToFocusOff ? " off!" : (checkedId == R.id.modeTapToFocusVisible) ? " on and visible!" : " on and invisible!"), Toast.LENGTH_SHORT).show();
        }
    };

    @OnTextChanged(value = R.id.width, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void widthChanged() {

    }

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

    @OnTextChanged(value = R.id.height)
    void heightChanged() {

    }

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

        camera.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCameraWidth = right - left;
                mCameraHeight = bottom - top;
                camera.removeOnLayoutChangeListener(this);
                widthChanged();
                heightChanged();
            }
        });
        camera.setLayoutParams(cameraLayoutParams);

        Toast.makeText(this, (updateWidth && updateHeight ? "Width and height" : updateWidth ? "Width" : "Height") + " updated!", Toast.LENGTH_SHORT).show();
    }

}
