package com.flurgle.camerakit.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.activity_main)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    @BindView(R.id.modeQuality)
    CheckBox modeQuality;

    @BindView(R.id.modeSpeed)
    CheckBox modeSpeed;

    @BindView(R.id.screenWidth)
    TextView screenWidth;

    @BindView(R.id.width)
    EditText width;

    @BindView(R.id.widthWrapContent)
    CheckBox widthWrapContent;

    @BindView(R.id.widthMatchParent)
    CheckBox widthMatchParent;

    @BindView(R.id.screenHeight)
    TextView screenHeight;

    @BindView(R.id.height)
    EditText height;

    int pictureMode = CameraKit.Constants.PICTURE_MODE_QUALITY;

    boolean blockInvalidation;

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

    @OnCheckedChanged(R.id.modeQuality)
    void modeQuality(CompoundButton buttonCompat, boolean checked) {
        modeSpeed.setChecked(false);
        if (checked) {
            camera.setPictureMode(CameraKit.Constants.PICTURE_MODE_QUALITY);
        }

        invalidateParameters();
    }

    @OnCheckedChanged(R.id.modeSpeed)
    void modeSpeed(CompoundButton buttonCompat, boolean checked) {
        modeQuality.setChecked(false);
        if (checked) {
            camera.setPictureMode(CameraKit.Constants.PICTURE_MODE_SPEED);
        }

        invalidateParameters();
    }

    @OnFocusChange({ R.id.width, R.id.height })
    void inputFocusChanged(View view, boolean f) {
        blockInvalidation = view.isFocused();
    }

    @OnTextChanged(value = R.id.width, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void widthChanged() {
        if (width.isFocused()) {
            new Handler().postDelayed(new UpdateCameraRunnable(width), 2000);
        }
    }

    @OnCheckedChanged(R.id.widthWrapContent)
    void widthWrapContent(CompoundButton buttonCompat, boolean checked) {
        if (checked) {
            ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            camera.setLayoutParams(layoutParams);

            widthMatchParent.setChecked(false);
        }

        invalidateParameters();
    }

    @OnCheckedChanged(R.id.widthMatchParent)
    void widthMatchParent(CompoundButton buttonCompat, boolean checked) {
        if (checked) {
            ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            camera.setLayoutParams(layoutParams);

            widthWrapContent.setChecked(false);
        }

        invalidateParameters();
    }

    @OnTextChanged(value = R.id.height, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void heightChanged() {
        if (height.isFocused()) {
            new Handler().postDelayed(new UpdateCameraRunnable(height), 2000);
        }
    }

    @OnCheckedChanged(R.id.heightWrapContent)
    void heightWrapContent() {
        invalidateParameters();
    }

    @OnCheckedChanged(R.id.heightMatchParent)
    void heightMatchParent() {
        invalidateParameters();
    }

    private void invalidateParameters() {
        if (blockInvalidation) return;

        blockInvalidation = true;

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

        blockInvalidation = false;
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
