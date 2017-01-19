package com.wonderkiln.camerakit.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraView;
import com.wonderkiln.camerakit.Constants;

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

    }

    @OnClick(R.id.captureVideo)
    void captureVideo() {

    }

    @OnClick(R.id.toggleCamera)
    void toggleCamera() {
        switch (camera.toggleFacing()) {
            case Constants.FACING_BACK:
                Toast.makeText(this, "Switched to back camera!", Toast.LENGTH_SHORT).show();
                break;

            case Constants.FACING_FRONT:
                Toast.makeText(this, "Switched to front camera!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.toggleFlash)
    void toggleFlash() {
        switch (camera.toggleFlash()) {
            case Constants.FLASH_ON:
                Toast.makeText(this, "Flash on!", Toast.LENGTH_SHORT).show();
                break;

            case Constants.FLASH_OFF:
                Toast.makeText(this, "Flash off!", Toast.LENGTH_SHORT).show();
                break;

            case Constants.FLASH_AUTO:
                Toast.makeText(this, "Flash auto!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnTextChanged(value = R.id.width, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void widthChanged() {

    }

    @OnCheckedChanged(R.id.widthWrapContent)
    void widthWrapContent(CompoundButton buttonCompat, boolean checked) {
        if (checked) {
            ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            camera.setLayoutParams(layoutParams);

            widthMatchParent.setChecked(false);
        }
    }

    @OnCheckedChanged(R.id.widthMatchParent)
    void widthMatchParent(CompoundButton buttonCompat, boolean checked) {
        if (checked) {
            ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            camera.setLayoutParams(layoutParams);

            widthWrapContent.setChecked(false);
        }
    }

    @OnTextChanged(value = R.id.height, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void heightChanged() {

    }

    @OnCheckedChanged(R.id.heightWrapContent)
    void heightWrapContent() {

    }

    @OnCheckedChanged(R.id.heightMatchParent)
    void heightMatchParent() {

    }

}
