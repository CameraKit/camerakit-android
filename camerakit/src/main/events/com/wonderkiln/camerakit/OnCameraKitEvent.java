package com.wonderkiln.camerakit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnCameraKitEvent {
    Class<? extends CameraKitEvent> value() default CameraKitEvent.class;
}
