package com.wonderkiln.camerakit;


public class CameraKitBarCodeDetect extends CameraKitEvent {
    private CameraKitBarCode barCode;

    public CameraKitBarCodeDetect(CameraKitBarCode barCode) {
        super(TYPE_BARCODE_DETECTED);
        this.barCode = barCode;
    }

    public CameraKitBarCode getBarCode() {
        return this.barCode;
    }
}
