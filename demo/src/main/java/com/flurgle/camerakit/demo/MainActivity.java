package com.flurgle.camerakit.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.activity_main)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    @BindView(R.id.modeCaptureQuality)
    RadioButton modeQuality;

    @BindView(R.id.modeCaptureSpeed)
    RadioButton modeSpeed;

    @BindView(R.id.screenWidth)
    TextView screenWidth;

    @BindView(R.id.width)
    EditText width;

    @BindView(R.id.widthWrapContent)
    RadioButton widthWrapContent;

    @BindView(R.id.widthMatchParent)
    RadioButton widthMatchParent;

    @BindView(R.id.screenHeight)
    TextView screenHeight;

    @BindView(R.id.height)
    EditText height;

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
                invalidateParameters();
            }
        });
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
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                new PreviewDialog(MainActivity.this, bitmap).show();
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
                PreviewDialog previewDialog = new PreviewDialog(MainActivity.this, video);
                previewDialog.show();
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

    @OnCheckedChanged({R.id.modeCaptureQuality, R.id.modeCaptureSpeed})
    void pictureModeChanged(CompoundButton buttonCompat, boolean checked) {
        camera.setPictureMode(
                modeQuality.isChecked() ?
                        CameraKit.Constants.PICTURE_MODE_QUALITY :
                        CameraKit.Constants.PICTURE_MODE_SPEED
        );
    }

    @OnTextChanged(value = R.id.width, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void widthChanged() {
        if (width.isFocused()) {
            new Handler().postDelayed(new UpdateCameraRunnable(width), 2000);
        }
    }

    @OnCheckedChanged({R.id.widthCustom, R.id.widthWrapContent, R.id.widthMatchParent})
    void widthModeChanged(CompoundButton buttonCompat, boolean checked) {

    }

    @OnTextChanged(value = R.id.height)
    void heightChanged() {
        if (height.isFocused()) {
            new Handler().postDelayed(new UpdateCameraRunnable(height), 2000);
        }
    }

    @OnCheckedChanged({R.id.heightCustom, R.id.heightWrapContent, R.id.heightMatchParent})
    void heightModeChanged() {

    }


    private void invalidateParameters() {
        if (!widthMatchParent.isChecked() && !widthWrapContent.isChecked()) {
            width.setText(String.valueOf(camera.getWidth()));
            width.setHint("pixels");
        } else if (widthMatchParent.isChecked()) {
            width.setHint("match_parent");
            width.setText("");
        } else if (widthWrapContent.isChecked()) {
            width.setHint("wrap_content");
            width.setText("");
        }

        height.setText(String.valueOf(camera.getHeight()));
    }

    private class UpdateCameraRunnable implements Runnable {

        private EditText editText;
        private String startText;

        public UpdateCameraRunnable(EditText editText) {
            this.startText = editText.getText().toString();
        }

        @Override
        public void run() {
            if (startText.equals(editText.getText().toString())) {
                ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
                switch (editText.getId()) {
                    case R.id.width:
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        break;

                    case R.id.height:
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        break;
                }

                camera.setLayoutParams(layoutParams);

            }
        }

    }

}
