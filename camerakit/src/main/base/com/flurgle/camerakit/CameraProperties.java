package com.flurgle.camerakit;

/**
 * Simple pojo containing various camera properties.
 */
public class CameraProperties {
    public final float verticalViewingAngle;
    public final float horizontalViewingAngle;

    public CameraProperties(float verticalViewingAngle, float horizontalViewingAngle) {
        this.verticalViewingAngle = verticalViewingAngle;
        this.horizontalViewingAngle = horizontalViewingAngle;
    }
}
