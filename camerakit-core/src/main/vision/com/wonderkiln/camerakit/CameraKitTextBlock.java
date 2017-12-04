package com.wonderkiln.camerakit;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.text.TextBlock;

public class CameraKitTextBlock {

    private TextBlock textBlock;

    CameraKitTextBlock(TextBlock textBlock) {
        this.textBlock = textBlock;
    }

    public String getText() {
        return textBlock.getValue();
    }

    public Rect getBoundingBox() {
        return textBlock.getBoundingBox();
    }

    public Point[] getCornerPoints() {
        return textBlock.getCornerPoints();
    }

    public String getLanguage() {
        return textBlock.getLanguage();
    }

}
