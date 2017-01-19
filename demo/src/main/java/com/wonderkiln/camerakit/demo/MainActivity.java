package com.wonderkiln.camerakit.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraView;
import com.wonderkiln.camerakit.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.activity_main)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    @BindView(R.id.screenWidth)
    TextView screenWidth;

    @BindView(R.id.screenHeight)
    TextView screenHeight;

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

        camera.start();
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

    @OnCheckedChanged(R.id.widthWrapContent)
    void widthWrapContent() {

    }

    @OnCheckedChanged(R.id.widthMatchParent)
    void widthMatchParent() {

    }

    @OnCheckedChanged(R.id.heightWrapContent)
    void heightWrapContent() {

    }

    @OnCheckedChanged(R.id.heightMatchParent)
    void heightMatchParent() {

    }


}
