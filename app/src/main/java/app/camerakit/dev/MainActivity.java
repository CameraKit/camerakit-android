package app.camerakit.dev;

import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import jpegkit.Jpeg;
import jpegkit.JpegImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    private View orientationView;
    private View imageShutterView;
    private View videoShutterView;

    private CameraKitView cameraKitView;

    private JpegImageView jpegImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraKitView = findViewById(R.id.camera_view);

        cameraKitView.setErrorListener(new CameraKitView.ErrorListener() {
            @Override
            public void onError(CameraKitView cameraKitView, CameraKitView.CameraException e) {
                e.printStackTrace();
            }
        });

        cameraKitView.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() { Log.d("MA", "Camera Opened"); }

            @Override
            public void onClosed() {
                Log.d("MA", "Camera Closed");
            }
        });

        cameraKitView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() { Log.d("MA", "Preview Started");}

            @Override
            public void onStop() { Log.d("MA", "Preview Stopped"); }
        });

        jpegImageView = findViewById(R.id.imageView);

        orientationView = findViewById(R.id.orientation_view);
        orientationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraKitView.getFacing() == CameraKit.FACING_BACK){
                    cameraKitView.setFacing(CameraKit.FACING_FRONT);
                }
                else{
                    cameraKitView.setFacing(CameraKit.FACING_BACK);
                }
            }
        });

        imageShutterView = findViewById(R.id.image_shutter_view);
        imageShutterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView view, final byte[] photo) {
                        Log.e("MA", "Callback Called");
                        final Jpeg jpeg = new Jpeg(photo);
                        jpegImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                jpegImageView.setJpeg(jpeg);
                                jpeg.release();
                            }
                        });
                    }
                });
            }
        });

        videoShutterView = findViewById(R.id.video_shutter_view);
        videoShutterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent secondActivity = new Intent(v.getContext(), SecondActivity.class);
                startActivity(secondActivity);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MA", "onStart");
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MA", "onResume");
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MA", "onPause");
        cameraKitView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MA", "onStop");
        cameraKitView.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}