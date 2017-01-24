package com.flurgle.camerakit.demo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewDialog extends Dialog {

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.video)
    VideoView videoView;

    private Bitmap bitmap;
    private File video;

    public PreviewDialog(@NonNull Context context, Bitmap bitmap) {
        super(context);
        this.bitmap = bitmap;
    }

    public PreviewDialog(@NonNull Context context, File video) {
        super(context);
        this.video = video;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_preview);
        ButterKnife.bind(this);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (video != null) {
            imageView.setVisibility(View.GONE);
            videoView.setVideoPath(video.getAbsolutePath());
            videoView.setMediaController(null);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });
            videoView.start();
        }
    }

}
