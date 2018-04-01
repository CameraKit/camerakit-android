package com.wonderkiln.camerakit;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;



public class BarcodeProcessor implements Detector.Processor<Barcode>{

    private EventDispatcher mEventDispatcher;
    private CameraKitEventCallback<CameraKitBarCodeDetect> callback;

    public BarcodeProcessor(EventDispatcher mEventDispatcher, CameraKitEventCallback<CameraKitBarCodeDetect> callback) {
        this.mEventDispatcher = mEventDispatcher;
        this.callback = callback;
    }

    @Override
    public void release() {
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {

        SparseArray<Barcode> detectedItems = detections.getDetectedItems();

        for(int i = 0; i < detectedItems.size(); ++i) {
            Barcode item = detectedItems.valueAt(i);

            if(item != null && item.rawValue!= null) {
                CameraKitBarCodeDetect event = new CameraKitBarCodeDetect(new CameraKitBarCode(item));
                this.mEventDispatcher.dispatch(event);
                this.callback.callback(event);
            }
        }
    }

}