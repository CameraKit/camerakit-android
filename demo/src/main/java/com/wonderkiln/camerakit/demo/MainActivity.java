package com.wonderkiln.camerakit.demo;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;
import com.wonderkiln.camerakit.ErrorListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.activityMain)
    DrawerLayout drawerLayout;

    @BindView(R.id.leftDrawer)
    ListView leftDrawer;

    ActionBarDrawerToggle drawerToggle;

    @BindView(R.id.contentFrame)
    ViewGroup parent;

    @BindView(R.id.camera)
    CameraView camera;

    private int mCameraWidth;
    private int mCameraHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupDrawerAndToolbar();

        camera.setErrorListener(new ErrorListener() {
            @Override
            public void onError(Exception e) {
                Log.d("", e.getLocalizedMessage());
            }

            @Override
            public void onEvent(String name, String details) {
                Log.d("", name + " -> " + details);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_main_about: {
                Drawable icon = ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
                new AlertDialog.Builder(this)
                        .setIcon(icon)
                        .setTitle(getString(R.string.about_dialog_title))
                        .setMessage(getString(
                                R.string.about_dialog_message,
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE,
                                com.wonderkiln.camerakit.BuildConfig.VERSION_NAME
                        ))
                        .setPositiveButton("DONE", null)
                        .show();
                return true;
            }

            case R.id.menu_main_github: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github)));
                startActivity(intent);
                return true;
            }

            case R.id.menu_main_website: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_website)));
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupDrawerAndToolbar() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            View toolbarView = getLayoutInflater().inflate(R.layout.action_bar, null, false);
            TextView titleView = toolbarView.findViewById(R.id.toolbar_title);
            titleView.setText(Html.fromHtml("<b>Camera</b>Kit"));

            getSupportActionBar().setCustomView(toolbarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        final List<Pair<String, String>> options = new ArrayList<>();
        options.add(new Pair<>("Demo Preview Scaling", "on"));
        options.add(new Pair<>("Preview Size", "match_parent x wrap_content"));
        options.add(new Pair<>("Capture Method", "preview frame"));
        options.add(new Pair<>("Crop to Preview Frame", "on"));

        ArrayAdapter adapter = new ArrayAdapter<Pair<String, String>>(this, R.layout.drawer_list_item, R.id.text1, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.text1);
                text1.setTextColor(Color.WHITE);

                TextView text2 = view.findViewById(R.id.text2);
                text2.setTextColor(Color.WHITE);

                text1.setText(options.get(position).first);
                text2.setText(options.get(position).second);
                return view;
            }
        };

        leftDrawer.setAdapter(adapter);
    }

    void captureVideo() {
        camera.setCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
            }
        });

        camera.startRecordingVideo();
        camera.postDelayed(new Runnable() {
            @Override
            public void run() {
                camera.stopRecordingVideo();
            }
        }, 3000);
    }

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


//    private void updateCamera(boolean updateWidth, boolean updateHeight) {
//        ViewGroup.LayoutParams cameraLayoutParams = camera.getLayoutParams();
//        int width = cameraLayoutParams.width;
//        int height = cameraLayoutParams.height;
//
//        if (updateWidth) {
//            switch (widthModeRadioGroup.getCheckedRadioButtonId()) {
//                case R.id.widthCustom:
//                    String widthInput = this.width.getText().toString();
//                    if (widthInput.length() > 0) {
//                        try {
//                            width = Integer.valueOf(widthInput);
//                        } catch (Exception e) {
//
//                        }
//                    }
//
//                    break;
//
//                case R.id.widthWrapContent:
//                    width = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    break;
//
//                case R.id.widthMatchParent:
//                    width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    break;
//            }
//        }
//
//        if (updateHeight) {
//            switch (heightModeRadioGroup.getCheckedRadioButtonId()) {
//                case R.id.heightCustom:
//                    String heightInput = this.height.getText().toString();
//                    if (heightInput.length() > 0) {
//                        try {
//                            height = Integer.valueOf(heightInput);
//                        } catch (Exception e) {
//
//                        }
//                    }
//                    break;
//
//                case R.id.heightWrapContent:
//                    height = ViewGroup.LayoutParams.WRAP_CONTENT;
//                    break;
//
//                case R.id.heightMatchParent:
//                    height = parent.getHeight();
//                    break;
//            }
//        }
//
//        cameraLayoutParams.width = width;
//        cameraLayoutParams.height = height;
//
//        camera.addOnLayoutChangeListener(this);
//        camera.setLayoutParams(cameraLayoutParams);
//
//        Toast.makeText(this, (updateWidth && updateHeight ? "Width and height" : updateWidth ? "Width" : "Height") + " updated!", Toast.LENGTH_SHORT).show();
//    }

}
