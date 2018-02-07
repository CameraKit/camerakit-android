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
import android.widget.Toast;

import com.camerakit.CameraFacing;
import com.camerakit.CameraPhotographer;
import com.camerakit.CameraView;
import com.camerakit.Photo;
import com.github.rubensousa.floatingtoolbar.FloatingToolbar;
import com.github.rubensousa.floatingtoolbar.FloatingToolbarMenuBuilder;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, FloatingToolbar.ItemClickListener {

    private CameraView cameraView;
    private Toolbar toolbar;
    private FloatingActionButton photoButton;
    private FloatingToolbar settingsToolbar;
    private FloatingActionButton settingsButton;

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

        settingsToolbar = findViewById(R.id.toolbarSettings);
        settingsToolbar.setClickListener(this);
        settingsToolbar.setMenu(new FloatingToolbarMenuBuilder(this)
                .addItem(R.id.action_addons, R.drawable.logomark_white)
                .addItem(R.id.action_facing, R.drawable.ic_facing)
                .addItem(R.id.action_dimens, R.drawable.ic_dimens)
                .addItem(R.id.action_close, R.drawable.ic_close)
                .build());

        settingsButton = findViewById(R.id.fabSettings);
        settingsToolbar.attachFab(settingsButton);
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

    @Override
    public void onItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_addons: {
                break;
            }

            case R.id.action_facing: {
                CameraFacing cameraFacing = cameraView.toggleFacing();
                if (cameraFacing == CameraFacing.BACK) {
                    Toast.makeText(this, "Switched cameraFacing to back.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Switched cameraFacing to front.", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.action_dimens: {
                break;
            }

            case R.id.action_close: {
                settingsToolbar.hide();
                break;
            }
        }
    }

    @Override
    public void onItemLongClick(MenuItem item) {

    }

    private View.OnClickListener photoOnClickListener = v -> {
        settingsToolbar.hide();

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

}
