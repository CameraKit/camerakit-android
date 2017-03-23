package com.flurgle.camerakit.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurgle.camerakit.AspectRatio;
import com.flurgle.camerakit.Size;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewActivity extends Activity {

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

        Bitmap bitmap = ResultHolder.getImage();
        if (bitmap == null) {
            finish();
            return;
        }

        imageView.setImageBitmap(rotateBitmap(bitmap));

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

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setRotate(ResultHolder.getCameraRotation());
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }
}
