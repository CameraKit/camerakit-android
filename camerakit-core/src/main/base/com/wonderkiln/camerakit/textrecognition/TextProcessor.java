package com.wonderkiln.camerakit.textrecognition;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitTextDetect;
import com.wonderkiln.camerakit.EventDispatcher;

public class TextProcessor implements Detector.Processor<TextBlock> {

    private EventDispatcher mEventDispatcher;
    private CameraKitEventCallback<CameraKitTextDetect> callback;

    public TextProcessor(EventDispatcher mEventDispatcher, CameraKitEventCallback<CameraKitTextDetect> callback) {
        this.mEventDispatcher = mEventDispatcher;
        this.callback = callback;
    }

    @Override
    public void release() { }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> detectedItems = detections.getDetectedItems();
        for(int i = 0; i < detectedItems.size(); ++i) {
            TextBlock item = detectedItems.valueAt(i);
            if (item != null && item.getValue() != null) {
                CameraKitTextDetect event = new CameraKitTextDetect(new CameraKitTextBlock(item));
                mEventDispatcher.dispatch(event);
                callback.callback(event);
            }
        }
    }

    public static class CameraKitTextBlock {

        private TextBlock textBlock;

        CameraKitTextBlock(TextBlock textBlock) {
            this.textBlock = textBlock;
        }

        public String getText(){
            return textBlock.getValue();
        }

        public Rect getBoundingBox() {
            return textBlock.getBoundingBox();
        }

        public Point[] getCornerPoints(){
            return textBlock.getCornerPoints();
        }

        public String getLanguage() {
            return textBlock.getLanguage();
        }
    }
}