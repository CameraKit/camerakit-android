package com.camerakit;


import android.hardware.Camera;

import java.util.List;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

class Flash1 extends Camera1.BaseApi implements CameraApi.FlashApi, CameraAttributes.FlashAttributes {

    Flash1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    @Override
    public CameraFuture off() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture oneShot() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(FLASH_MODE_ON);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture torch() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        });
    }

    // *** Attributes

    @Override
    public boolean canOneShotFlash() {
        List<String> flashModes = mCameraParameters.getSupportedFlashModes();
        if (flashModes == null) {
            return false;
        }

        for (String flashMode : flashModes) {
            if (FLASH_MODE_ON.equals(flashMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canTorchFlash() {
        List<String> flashModes = mCameraParameters.getSupportedFlashModes();
        if (flashModes == null) {
            return false;
        }

        for (String flashMode : flashModes) {
            if (FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
        }

        return false;
    }

}
