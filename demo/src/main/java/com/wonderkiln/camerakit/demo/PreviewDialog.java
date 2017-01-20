package com.wonderkiln.camerakit.demo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewDialog extends Dialog {

    @BindView(R.id.image)
    ImageView image;

    private Bitmap bitmap;

    public PreviewDialog(@NonNull Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_preview);
        ButterKnife.bind(this);

        image.setImageBitmap(bitmap);
    }

}
