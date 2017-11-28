package com.wonderkiln.camerakit;

import com.wonderkiln.camerakit.textrecognition.TextProcessor.CameraKitTextBlock;

public class CameraKitTextDetect extends CameraKitEvent {

    private CameraKitTextBlock textBlock;

    public CameraKitTextDetect(CameraKitTextBlock textBlock) {
        super(TYPE_TEXT_DETECTED);
        this.textBlock = textBlock;
    }

    public CameraKitTextBlock getTextBlock() {
        return textBlock;
    }
}
