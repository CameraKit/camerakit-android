package com.wonderkiln.camerakit;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.TextBlock;

/**
 * Did
 */

public class CameraKitBarCode {


        private Barcode barCode;

        CameraKitBarCode(Barcode barCode) {
            this.barCode = barCode;
        }

        public String getRawValue() {
            return barCode.rawValue;
        }

        public Rect getBoundingBox() {
            return barCode.getBoundingBox();
        }

        public Point[] getCornerPoints() {
            return barCode.cornerPoints;
        }

        public int getFormat() {
            return barCode.format;
        }



}
