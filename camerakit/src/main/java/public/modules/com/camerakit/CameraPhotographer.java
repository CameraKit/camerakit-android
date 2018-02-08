package com.camerakit;

import java.util.List;

public class CameraPhotographer extends CameraModule {

    public Photo capture() {
        if (mCameraView == null || photoApi() == null) {
            return null;
        }

        Photo photo = new Photo(mCameraView.getContext());

        int jpegQuality = chooseJpegQuality();
        CameraSize size = chooseCaptureSize();

        CameraApi.PhotoApi photoApi = photoApi();
        CameraApi.PreviewApi previewApi = previewApi();

        photoApi.setJpegQuality(jpegQuality)
                .then(() -> photoApi.setSize(size.getWidth(), size.getHeight()))
                .success(() -> {
                    photoApi.captureStandard()
                            .result(photo::set)
                            .error(photo::set)
                            .then(previewApi::start);
                })
                .error(photo::set);

        return photo;
    }

    public CameraSize chooseCaptureSize() {
        List<CameraSize> sizes = mCameraApi.photoAttributes().supportedSizes();
        return sizes.get(0);
    }

    public int chooseJpegQuality() {
        return 100;
    }

    protected CameraApi.PhotoApi photoApi() {
        if (mCameraApi != null) {
            return mCameraApi.photoApi();
        }

        return null;
    }

    protected CameraAttributes.PhotoAttributes photoAttributes() {
        if (mCameraApi != null) {
            return mCameraApi.photoAttributes();
        }

        return null;
    }

    protected CameraApi.PreviewApi previewApi() {
        if (mCameraApi != null) {
            return mCameraApi.previewApi();
        }

        return null;
    }

}
