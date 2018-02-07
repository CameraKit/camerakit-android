package com.camerakit;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

public class CameraFacing {

    public static final CameraFacing FRONT = new CameraFacing();
    public static final CameraFacing BACK = new CameraFacing();

    private CameraFacing() {
    }

    public static CameraFacing get(int cameraId) {
        switch (cameraId) {
            case CAMERA_FACING_BACK:
                return BACK;
            case CAMERA_FACING_FRONT:
                return FRONT;
            default:
                return BACK;
        }
    }

}
