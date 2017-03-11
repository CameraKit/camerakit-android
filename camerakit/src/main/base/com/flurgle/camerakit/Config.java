package com.flurgle.camerakit;

public class Config {

    public boolean mMirrorYAxisForFrontCamera = false;

    private Config() {

    }

    public static class Builder {

        private Config config;

        public Builder() {
            config = new Config();
        }

        public Builder setMirrorYAxisForFrontCamera(boolean mirrorYAxisForFrontCamera) {
            config.mMirrorYAxisForFrontCamera = mirrorYAxisForFrontCamera;
            return this;
        }

        public Config build() {
            return config;
        }
    }
}
