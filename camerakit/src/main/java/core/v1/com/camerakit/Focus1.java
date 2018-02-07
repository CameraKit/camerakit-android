package com.camerakit;


import android.graphics.Rect;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_EDOF;
import static android.hardware.Camera.Parameters.FOCUS_MODE_MACRO;

class Focus1 extends Camera1.BaseApi implements CameraApi.FocusApi, CameraAttributes.FocusAttributes {

    Focus1(CameraExecutor cameraExecutor) {
        super(cameraExecutor);
    }

    // *** Api

    @Override
    public CameraFuture clearAreas() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusAreas(new ArrayList<>());
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture addArea(float x, float y, int radius, int weight) {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Area> areas = parameters.getFocusAreas();
            if (areas == null) {
                areas = new ArrayList<>();
            }

            if (maxFocusAreas() <= 0) {
                return;
            }

            if (areas.size() == maxFocusAreas()) {
                areas.remove(0);
            }

            int areaX = (int) ((x * 2000) - 1000);
            int areaY = (int) ((y * 2000) - 1000);

            int left = areaX - radius;
            if (left < -1000) left = -1000;

            int top = areaY - radius;
            if (top < -1000) left = -1000;

            int right = areaX + radius;
            if (right > 1000) right = 1000;

            int bottom = areaY + radius;
            if (bottom > 1000) bottom = 1000;

            Rect rect = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(rect, weight));
            parameters.setFocusAreas(areas);

            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture<Boolean> autoFocus() {
        return new CameraFuture<>(mCameraExecutor, (cameraFuture) -> {
            mCamera.autoFocus((success, camera) -> {
                cameraFuture.complete(success);
            });
        });
    }

    @Override
    public CameraFuture modeAuto() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture modeContinuousPhoto() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture modeContinuousVideo() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture modeMacro() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(FOCUS_MODE_MACRO);
            mCamera.setParameters(parameters);
        });
    }

    @Override
    public CameraFuture modeEdof() {
        return new CameraFuture(mCameraExecutor, () -> {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(FOCUS_MODE_EDOF);
            mCamera.setParameters(parameters);
        });
    }

    // *** Attributes

    @Override
    public boolean canAutoFocus() {
        List<String> focusModes = mCameraParameters.getSupportedFocusModes();
        for (String focusMode : focusModes) {
            if (FOCUS_MODE_AUTO.equals(focusMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinuousPhotoFocus() {
        List<String> focusModes = mCameraParameters.getSupportedFocusModes();
        for (String focusMode : focusModes) {
            if (FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinuousVideoFocus() {
        List<String> focusModes = mCameraParameters.getSupportedFocusModes();
        for (String focusMode : focusModes) {
            if (FOCUS_MODE_CONTINUOUS_VIDEO.equals(focusMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canMacroFocus() {
        List<String> focusModes = mCameraParameters.getSupportedFocusModes();
        for (String focusMode : focusModes) {
            if (FOCUS_MODE_MACRO.equals(focusMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canEdofFocus() {
        List<String> focusModes = mCameraParameters.getSupportedFocusModes();
        for (String focusMode : focusModes) {
            if (FOCUS_MODE_EDOF.equals(focusMode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int maxFocusAreas() {
        return mCameraParameters.getMaxNumFocusAreas();
    }

}
