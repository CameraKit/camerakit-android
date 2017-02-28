package com.flurgle.camerakit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CommonAspectRatioFilter {

    private List<Size> mPreviewSizes;
    private List<Size> mCaptureSizes;

    public CommonAspectRatioFilter(List<Size> previewSizes, List<Size> captureSizes) {
        this.mPreviewSizes = previewSizes;
        this.mCaptureSizes = captureSizes;
    }

    public TreeSet<AspectRatio> filter() {
        Set<AspectRatio> previewAspectRatios = new HashSet<>();
        for (Size size : mPreviewSizes) {
            if (size.getWidth() >= CameraKit.Internal.screenHeight && size.getHeight() >= CameraKit.Internal.screenWidth) {
                previewAspectRatios.add(AspectRatio.of(size.getWidth(), size.getHeight()));
            }
        }

        Set<AspectRatio> captureAspectRatios = new HashSet<>();
        for (Size size : mCaptureSizes) {
            captureAspectRatios.add(AspectRatio.of(size.getWidth(), size.getHeight()));
        }

        TreeSet<AspectRatio> output = new TreeSet<>();
        for (AspectRatio aspectRatio : previewAspectRatios) {
            if (captureAspectRatios.contains(aspectRatio)) {
                output.add(aspectRatio);
            }
        }

        return output;
    }

}
