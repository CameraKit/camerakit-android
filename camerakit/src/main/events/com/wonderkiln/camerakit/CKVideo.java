package com.wonderkiln.camerakit;

import java.io.File;

public class CKVideo extends CKEvent {

    private File videoFile;

    CKVideo(File videoFile) {
        super(TYPE_VIDEO_CAPTURED);
        this.videoFile = videoFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

}
