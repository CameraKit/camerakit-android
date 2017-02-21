package com.flurgle.camerakit;

import android.content.res.Resources;

public class CameraKit {

    static class Internal {

        static final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        static final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    }

    public static class Constants {

        public static final int FACING_BACK = 0;
        public static final int FACING_FRONT = 1;

        public static final int FLASH_OFF = 0;
        public static final int FLASH_ON = 1;
        public static final int FLASH_AUTO = 2;

        public static final int CAPTURE_MODE_STANDARD = 0;
        public static final int CAPTURE_MODE_STILL = 1;
        public static final int PICTURE_MODE_AUTO = 2;

        public static final int TAP_TO_FOCUS_VISIBLE = 0;
        public static final int TAP_TO_FOCUS_INVISIBLE = 1;
        public static final int TAP_TO_FOCUS_OFF = 2;

    }


}
