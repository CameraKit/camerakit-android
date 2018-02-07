package com.camerakit.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraPhotographer;
import com.camerakit.CameraView;
import com.camerakit.Photo;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private CameraView cameraView;
    private Toolbar toolbar;
    private FloatingActionButton photoButton;

    private Button previewSettingsButton;
    private Button photoSettingsButton;
    private Button flashlightButton;
    private Button facingButton;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        cameraView.stop();
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
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return false;
    }

    private View.OnClickListener photoOnClickListener = v -> {
        CameraPhotographer photographer = new CameraPhotographer();
        cameraView.use(photographer);

        Photo photo = photographer.capture();
        photo.toGalleryFile()
                .whenReady(photoFile -> {
                    Toast.makeText(this, "Saved Photo to Gallery!", Toast.LENGTH_SHORT).show();
                })
                .catchError(error -> {

                });
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
