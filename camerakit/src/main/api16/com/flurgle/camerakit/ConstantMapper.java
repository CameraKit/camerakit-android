package com.flurgle.camerakit;

import android.hardware.Camera;
import android.support.v4.util.SparseArrayCompat;

public class ConstantMapper {

    private abstract static class BaseMapper<T> {

        protected int mCameraKitConstant;

        protected BaseMapper(int cameraKitConstant) {
            this.mCameraKitConstant = cameraKitConstant;
        }

        abstract T map();

    }

    public static class Flash extends BaseMapper<String> {

        private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

        static {
            FLASH_MODES.put(CameraKit.Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
            FLASH_MODES.put(CameraKit.Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
            FLASH_MODES.put(CameraKit.Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        }

        protected Flash(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        String map() {
            return FLASH_MODES.get(mCameraKitConstant, FLASH_MODES.get(CameraKit.Constants.FLASH_OFF));
        }

    }

    public static class Facing extends BaseMapper<Integer> {

        private static final SparseArrayCompat<Integer> FACING_MODES = new SparseArrayCompat<>();

        static {
            FACING_MODES.put(CameraKit.Constants.FACING_BACK, Camera.CameraInfo.CAMERA_FACING_BACK);
            FACING_MODES.put(CameraKit.Constants.FACING_FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        protected Facing(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        Integer map() {
            return FACING_MODES.get(mCameraKitConstant, FACING_MODES.get(Camera.CameraInfo.CAMERA_FACING_BACK));
        }

    }


}
