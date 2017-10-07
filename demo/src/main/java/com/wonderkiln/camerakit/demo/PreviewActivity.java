package com.wonderkiln.camerakit.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wonderkiln.camerakit.AspectRatio;
import com.wonderkiln.camerakit.Size;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends AppCompatActivity {

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.nativeCaptureResolution)
    TextView nativeCaptureResolution;

    @BindView(R.id.actualResolution)
    TextView actualResolution;

    @BindView(R.id.approxUncompressedSize)
    TextView approxUncompressedSize;

    @BindView(R.id.captureLatency)
    TextView captureLatency;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        setupToolbar();

        Bitmap bitmap = ResultHolder.getImage();
        if (bitmap == null) {
            finish();
            return;
        }

        imageView.setImageBitmap(bitmap);

        Size captureSize = ResultHolder.getNativeCaptureSize();
        if (captureSize != null) {
            // Native sizes are landscape, hardcode flip because demo app forced to portrait.
            AspectRatio aspectRatio = AspectRatio.of(captureSize.getHeight(), captureSize.getWidth());
            nativeCaptureResolution.setText(captureSize.getHeight() + " x " + captureSize.getWidth() + " (" + aspectRatio.toString() + ")");
        }

        actualResolution.setText(bitmap.getWidth() + " x " + bitmap.getHeight());
        approxUncompressedSize.setText(getApproximateFileMegabytes(bitmap) + "MB");
        captureLatency.setText(ResultHolder.getTimeToCallback() + " milliseconds");
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            View toolbarView = getLayoutInflater().inflate(R.layout.action_bar, null, false);
            TextView titleView = toolbarView.findViewById(R.id.toolbar_title);
            titleView.setText(Html.fromHtml("<b>Camera</b>Kit"));

            getSupportActionBar().setCustomView(toolbarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }

}
