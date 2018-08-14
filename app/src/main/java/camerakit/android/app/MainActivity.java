package camerakit.android.app;

import android.app.Activity;
import android.os.Bundle;

import camerakit.android.CameraKitView;

public class MainActivity extends Activity {

    private CameraKitView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera);
    }

    @Override
    protected void onStart() {
        super.onStart();
        camera.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.onStop();
    }

}
