package com.flurgle.camerakit.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.flurgle.camerakit.demo.R.id.video;

public class PreviewActivity extends Activity {

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(video)
    VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        Bitmap bitmap = MediaHolder.getImage();
        File video = MediaHolder.getVideo();

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
