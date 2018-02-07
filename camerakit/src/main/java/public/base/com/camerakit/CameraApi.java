package com.camerakit;

import android.view.SurfaceHolder;

public interface CameraApi extends CameraAttributes {

    CameraFuture connect();
    CameraFuture disconnect();

    PreviewApi previewApi();
    FocusApi focusApi();
    ZoomApi zoomApi();
    FlashApi flashApi();
    PhotoApi photoApi();
    VideoApi videoApi();

    interface PreviewApi {
        CameraFuture setDisplayOrientation(int displayOrientation);
        CameraFuture setSize(int width, int height);
        CameraFuture setSurface(SurfaceHolder surfaceHolder);

        CameraFuture start();
        CameraFuture stop();
    }

    interface FocusApi {
        CameraFuture clearAreas();
        CameraFuture addArea(float x, float y, int radius, int weight);

        CameraFuture<Boolean> autoFocus();

        CameraFuture modeAuto();
        CameraFuture modeContinuousPhoto();
        CameraFuture modeContinuousVideo();
        CameraFuture modeMacro();
        CameraFuture modeEdof();
    }

    interface ZoomApi {
        CameraFuture zoom(float factor);
        CameraFuture smoothZoom(float factor);
    }

    interface FlashApi {
        CameraFuture off();
        CameraFuture oneShot();
        CameraFuture torch();
    }

    interface PhotoApi {
        CameraFuture setSize(int width, int height);
        CameraFuture setJpegQuality(int jpegQuality);

        CameraFuture<byte[]> captureStandard();
        CameraFuture<byte[]> capturePreview();
    }

    interface VideoApi {
    }

}
