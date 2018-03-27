package com.camerakit.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.jpegkit.JpegFile;
import com.jpegkit.JpegImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private CameraKitView cameraView;
    private Toolbar toolbar;
    private FloatingActionButton photoButton;

    private Button previewSettingsButton;
    private Button photoSettingsButton;
    private Button flashlightButton;
    private Button facingButton;

    private JpegImageView newPhotoImageView;
    private JpegImageView photoImageView;

    private File mPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(this);

        photoButton = findViewById(R.id.fabPhoto);
        photoButton.setOnClickListener(photoOnClickListener);

        previewSettingsButton = findViewById(R.id.previewSettingsButton);
        previewSettingsButton.setOnClickListener(previewSettingsOnClickListener);

        photoSettingsButton = findViewById(R.id.photoSettingsButton);
        photoSettingsButton.setOnClickListener(photoSettingsOnClickListener);

        flashlightButton = findViewById(R.id.flashlightButton);
        flashlightButton.setOnClickListener(flashlightOnClickListener);

        facingButton = findViewById(R.id.facingButton);
        facingButton.setOnClickListener(facingOnClickListener);

        newPhotoImageView = findViewById(R.id.newImageView);
        newPhotoImageView.setVisibility(View.GONE);

        photoImageView = findViewById(R.id.photoImageView);
        photoImageView.setAlpha(0f);
        photoImageView.setOnClickListener((v -> {
            if (mPhoto != null) {
                MediaScannerConnection.scanFile(this, new String[]{mPhoto.getAbsolutePath()}, null,
                        (path, uri) -> {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "image/*");
                            startActivity(intent);
                        });
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_about) {
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.about_dialog_title)
                    .setMessage(R.string.about_dialog_message)
                    .setNeutralButton("Dismiss", null)
                    .show();

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#91B8CC"));
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(Html.fromHtml("<b>Dismiss</b>"));

            return true;
        }

        if (item.getItemId() == R.id.main_menu_gallery) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivity(intent);
            return true;
        }

        return false;
    }

    private View.OnClickListener photoOnClickListener = v -> {
        cameraView.capturePhoto(((view, photo) -> {
            photo.saveToGallery(view.getContext(), "test.jpg", new CameraKitView.Photo.JpegFileCallback() {
                @Override
                public void onJpegFile(JpegFile jpegFile) {
                    mPhoto = jpegFile.getFile();
                }

                @Override
                public void onError(CameraKitView.CameraException error) {
                    Log.e("CameraKit", error.toString());
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, photo.getJpeg().getWidth() + "x" + photo.getJpeg().getHeight(), Toast.LENGTH_SHORT).show();

                    newPhotoImageView.setJpeg(photo.getJpeg());
                    newPhotoImageView.setAlpha(0f);
                    newPhotoImageView.setScaleX(1f);
                    newPhotoImageView.setScaleY(1f);
                    newPhotoImageView.setVisibility(View.VISIBLE);

                    newPhotoImageView.animate()
                            .alpha(1f)
                            .scaleX(0.1f)
                            .scaleY(0.1f)
                            .setDuration(450)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    photoImageView.setAlpha(1f);
                                    photoImageView.setJpeg(photo.getJpeg());

                                    newPhotoImageView.setVisibility(View.GONE);
                                }
                            })
                            .start();
                }
            });
        }));
    };


    private View.OnClickListener previewSettingsOnClickListener = v -> {

    };

    private View.OnClickListener photoSettingsOnClickListener = v -> {

    };

    private View.OnClickListener flashlightOnClickListener = v -> {

    };

    private View.OnClickListener facingOnClickListener = v -> {
        cameraView.toggleFacing();
    };

}
