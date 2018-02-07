package com.camerakit;

import java.util.List;

public interface CameraAttributes {

    HardwareAttributes hardwareAttributes();
    PreviewAttributes previewAttributes();
    FocusAttributes focusAttributes();
    ZoomAttributes zoomAttributes();
    FlashAttributes flashAttributes();
    PhotoAttributes photoAttributes();
    VideoAttributes videoAttributes();

    interface HardwareAttributes {
        CameraFacing facing();
        int orientation();
    }

    interface PreviewAttributes {
        List<CameraSize> supportedSizes();
    }

    interface FocusAttributes {
        boolean canAutoFocus();
        boolean canContinuousPhotoFocus();
        boolean canContinuousVideoFocus();
        boolean canMacroFocus();
        boolean canEdofFocus();
        int maxFocusAreas();
    }

    interface ZoomAttributes {
        boolean canZoom();
        boolean canSmoothZoom();
        float maxZoom();
        float[] supportedZooms();
    }

    interface FlashAttributes {
        boolean canOneShotFlash();
        boolean canTorchFlash();
    }

    interface PhotoAttributes {
        List<CameraSize> supportedSizes();
        boolean canDisableShutterSound();
    }

    interface VideoAttributes {
        boolean canRecordVideo();
        CameraSize preferredSize();
        List<CameraSize> supportedSizes();
    }

}
