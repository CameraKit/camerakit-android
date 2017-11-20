package com.wonderkiln.camerakit;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

public class TextProcessor implements Detector.Processor<TextBlock> {

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> detectedItems = detections.getDetectedItems();
        for(int i = 0; i < detectedItems.size(); ++i) {
            TextBlock item = detectedItems.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("WHAT", "Text Detected: " + item.getValue());
            }
        }
    }
}