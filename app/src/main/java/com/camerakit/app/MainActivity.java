package com.camerakit.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.jpegkit.Jpeg;
import com.jpegkit.JpegImageView;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private CameraKitView cameraView;
    private Toolbar toolbar;
    private Menu menu;

    private Button photoButton;
    private Button flashButton;
    private Button facingButton;

    private Button permissionsButton;

    private MenuItem flashState;

    private JpegImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(this);
        
        menu = toolbar.getMenu();

        flashState = menu.findItem(R.id.main_menu_flash);

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);

        flashButton = findViewById(R.id.flashButton);
        flashButton.setOnClickListener(flashOnClickListener);

        facingButton = findViewById(R.id.facingButton);
        facingButton.setOnClickListener(facingOnClickListener);

        permissionsButton = findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.requestPermissions(MainActivity.this);
            }
        });

        imageView = findViewById(R.id.imageView);

        cameraView.setPermissionsListener(new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                permissionsButton.setVisibility(View.GONE);
            }

            @Override
            public void onPermissionsFailure() {
                permissionsButton.setVisibility(View.VISIBLE);
            }
        });
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

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView vew, final byte[] photo) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Jpeg jpeg = new Jpeg(photo);
                            imageView.setJpeg(jpeg);
                        }
                    });
                }
            });
        }
    };

    private View.OnClickListener flashOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cameraView.getFlash() == CameraKit.FLASH_OFF) {
                cameraView.setFlash(CameraKit.FLASH_ON);
                flashState.setIcon(R.drawable.ic_flash);
            } else if (cameraView.getFlash() == CameraKit.FLASH_ON){
                cameraView.setFlash(CameraKit.FLASH_TORCH);
                flashState.setIcon(R.drawable.ic_torch);
            } else {
                cameraView.setFlash(CameraKit.FLASH_OFF);
                flashState.setIcon(R.drawable.ic_flash_off);
            }
        }
    };

    private View.OnClickListener facingOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraView.toggleFacing();
        }
    };

}
