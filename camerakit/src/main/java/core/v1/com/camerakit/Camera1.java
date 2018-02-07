package com.camerakit;

import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

class Camera1 implements CameraApi {

    private int mCameraId;
    private Camera mCamera;
    private CameraExecutor mCameraExecutor;

    private Preview1 mPreview;
    private Focus1 mFocus;
    private Zoom1 mZoom;
    private Flash1 mFlash;
    private Photo1 mPhoto;
    private Video1 mVideo;

    private Camera1() {
    }

    Camera1(CameraFacing facing, CameraExecutor cameraExecutor) {
        mCameraExecutor = cameraExecutor;

        if (facing == CameraFacing.BACK) {
            mCameraId = CAMERA_FACING_BACK;
        } else {
            mCameraId = CAMERA_FACING_FRONT;
        }

        mPreview = new Preview1(mCameraExecutor);
        mFocus = new Focus1(mCameraExecutor);
        mZoom = new Zoom1(mCameraExecutor);
        mFlash = new Flash1(mCameraExecutor);
        mPhoto = new Photo1(mCameraExecutor);
        mVideo = new Video1(mCameraExecutor);
    }

    @Override
    public CameraFuture connect() {
        return new CameraFuture(mCameraExecutor, () -> {
            mCamera = Camera.open(mCameraId);

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, cameraInfo);
            Camera.Parameters cameraParameters = mCamera.getParameters();

            mPreview.onCameraConnected(mCamera, cameraInfo, cameraParameters);
            mFocus.onCameraConnected(mCamera, cameraInfo, cameraParameters);
            mZoom.onCameraConnected(mCamera, cameraInfo, cameraParameters);
            mFlash.onCameraConnected(mCamera, cameraInfo, cameraParameters);
            mPhoto.onCameraConnected(mCamera, cameraInfo, cameraParameters);
            mVideo.onCameraConnected(mCamera, cameraInfo, cameraParameters);
        });
    }

    @Override
    public CameraFuture disconnect() {
        return new CameraFuture(mCameraExecutor, () -> {
            mCamera.release();
            mCamera = null;

            mPreview.onCameraDisconnected();
            mFocus.onCameraDisconnected();
            mZoom.onCameraDisconnected();
            mFlash.onCameraDisconnected();
            mPhoto.onCameraDisconnected();
            mVideo.onCameraDisconnected();
        });
    }

    @Override
    public HardwareAttributes hardwareAttributes() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);

        return new HardwareAttributes() {
            @Override
            public CameraFacing facing() {
                return CameraFacing.get(cameraInfo.facing);
            }

            @Override
            public int orientation() {
                return cameraInfo.orientation;
            }
        };
    }

    @Override
    public PreviewApi previewApi() {
        return mPreview;
    }

    @Override
    public PreviewAttributes previewAttributes() {
        return mPreview;
    }

    @Override
    public FocusApi focusApi() {
        return mFocus;
    }

    @Override
    public FocusAttributes focusAttributes() {
        return mFocus;
    }

    @Override
    public ZoomApi zoomApi() {
        return mZoom;
    }

    @Override
    public ZoomAttributes zoomAttributes() {
        return mZoom;
    }

    @Override
    public FlashApi flashApi() {
        return mFlash;
    }

    @Override
    public FlashAttributes flashAttributes() {
        return mFlash;
    }

    @Override
    public PhotoApi photoApi() {
        return mPhoto;
    }

    @Override
    public PhotoAttributes photoAttributes() {
        return mPhoto;
    }

    @Override
    public VideoApi videoApi() {
        return mVideo;
    }

    @Override
    public VideoAttributes videoAttributes() {
        return mVideo;
    }

    static abstract class BaseApi {

        protected CameraExecutor mCameraExecutor;

        protected Camera mCamera;
        protected Camera.CameraInfo mCameraInfo;
        protected Camera.Parameters mCameraParameters;

        private BaseApi() {
        }

        BaseApi(CameraExecutor cameraExecutor) {
            mCameraExecutor = cameraExecutor;
        }

        void onCameraConnected(Camera camera, Camera.CameraInfo cameraInfo, Camera.Parameters cameraParameters) {
            mCamera = camera;
            mCameraInfo = cameraInfo;
            mCameraParameters = cameraParameters;
        }

        void onCameraDisconnected() {
            mCamera = null;
            mCameraInfo = null;
            mCameraParameters = null;
        }

        protected static List<CameraSize> convertSizeList(List<Camera.Size> sizes) {
            List<CameraSize> conversion = new ArrayList<>();
            for (Camera.Size size : sizes) {
                conversion.add(new CameraSize(size.width, size.height));
            }

            return conversion;
        }

    }

}
