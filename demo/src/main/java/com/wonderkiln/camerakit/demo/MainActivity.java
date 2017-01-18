package com.wonderkiln.camerakit.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wonderkiln.camerakit.CameraView;

public class MainActivity extends AppCompatActivity {

    private CameraView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = (CameraView) findViewById(R.id.camera);
        camera.start();
    }

}
