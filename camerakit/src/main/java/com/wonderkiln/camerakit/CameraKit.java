package com.wonderkiln.camerakit;

import android.content.res.Resources;

public class CameraKit {

    static class Internal {

        static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    }

    public static class Constants {

        public static final int PERMISSION_REQUEST_CAMERA = 16;

        public static final int FACING_BACK = 0;
        public static final int FACING_FRONT = 1;

        public static final int FLASH_OFF = 0;
        public static final int FLASH_ON = 1;
        public static final int FLASH_AUTO = 2;
        public static final int FLASH_TORCH = 3;

        public static final int FOCUS_OFF = 0;
        public static final int FOCUS_CONTINUOUS = 1;
        public static final int FOCUS_TAP = 2;
        public static final int FOCUS_TAP_WITH_MARKER = 3;

        public static final int METHOD_STANDARD = 0;
        public static final int METHOD_STILL = 1;

        public static final int PERMISSIONS_STRICT = 0;
        public static final int PERMISSIONS_LAZY = 1;
        public static final int PERMISSIONS_PICTURE = 2;

        public static final int VIDEO_QUALITY_480P = 0;
        public static final int VIDEO_QUALITY_720P = 1;
        public static final int VIDEO_QUALITY_1080P = 2;
        public static final int VIDEO_QUALITY_2160P = 3;
        public static final int VIDEO_QUALITY_HIGHEST = 4;
        public static final int VIDEO_QUALITY_LOWEST = 5;
        public static final int VIDEO_QUALITY_QVGA = 6;

    }

    static class Defaults {

        static final int DEFAULT_FACING = Constants.FACING_BACK;
        static final int DEFAULT_FLASH = Constants.FLASH_OFF;
        static final int DEFAULT_FOCUS = Constants.FOCUS_CONTINUOUS;
        static final boolean DEFAULT_PINCH_TO_ZOOM = true;
        static final float DEFAULT_ZOOM = 1.f;
        static final int DEFAULT_METHOD = Constants.METHOD_STANDARD;
        static final int DEFAULT_PERMISSIONS = Constants.PERMISSIONS_STRICT;
        static final int DEFAULT_VIDEO_QUALITY = Constants.VIDEO_QUALITY_480P;

        static final int DEFAULT_JPEG_QUALITY = 100;
        static final int DEFAULT_VIDEO_BIT_RATE = 0;
        static final boolean DEFAULT_CROP_OUTPUT = false;
        static final boolean DEFAULT_DOUBLE_TAP_TO_TOGGLE_FACING = false;
        static final boolean DEFAULT_ADJUST_VIEW_BOUNDS = false;

    }

}
