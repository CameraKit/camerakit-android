![CameraKit Header](.repo/camerakit-android-header.png)

CameraKit is an extraordinarily easy to use utility to work with the infamous Android Camera and Camera2 APIs. Built by [Dylan McIntyre](https://github.com/dwillmc).


Try out all the unique features using the CameraKit Demo from the Google Play store!

<a href='https://play.google.com/store/apps/details?id=com.flurgle.camerakit.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80'/></a>


## Table of Contents
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
  - [Events](#events)
  - [Extra Attributes](#extra-attributes)
    - [`ckCropOutput`](#ckcropoutput)
    - [`ckFacing`](#ckfacing)
    - [`ckFlash`](#ckflash)
    - [`ckPictureMode`](#ckpicturemode)
  - [Capturing Images](#capturing-images)
  - [Capturing Video](#capturing-video)
- [Automatic Permissions Behavior](#automatic-permissions-behavior)
- [Dynamic Behavior](#dynamic-sizing-behavior)
- [Capture Mode Behavior](#capture-mode-behavior)
- [Credits](#credits)
- [License](#license)

## Features

- Image and video capture seamlessly working with the same preview session.
- Automatic system permission handling.
- Automatic preview scaling.
  - Create a `CameraView` of any size (not just presets!).
  - Automatic output cropping to match your `CameraView` bounds.
- Multiple capture modes.
  - `PICTURE_MODE_QUALITY`: an image captured normally using the camera APIs.
  - `PICTURE_MODE_SPEED`: a freeze frame of the `CameraView` preview (similar to SnapChat and Instagram) for devices with slower cameras.
  - Automatic picture mode determination based on measured speed.

## Setup
Add __CameraKit__ to the dependencies block in your `app` level `build.gradle`:
```groovy
compile 'com.flurgle:camerakit:1.0.0'
```

## Usage

To use CameraKit, simply add a `CameraView` to your layout:

```xml
<com.flurgle.camerakit.CameraView
    android:id="@+id/camera"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Make sure you override `onResume` and `onPause` in your activity, and make calls respectively to `CameraView.start()` and `CameraView.stop()`.

```java
@Override
protected void onResume() {
    super.onResume();
    cameraView.start();
}

@Override
protected void onPause() {
    cameraView.stop();
    super.onPause();
}
```

### Events

Make sure you can react to different camera events by setting up a `CameraListener` instance.

```java
camera.setCameraListener(new CameraListener() {

    @Override
    public void onCameraOpened() {
        super.onCameraOpened();
    }

    @Override
    public void onCameraClosed() {
        super.onCameraClosed();
    }

    @Override
    public void onPictureTaken(byte[] picture) {
        super.onPictureTaken(picture);
    }

    @Override
    public void onVideoTaken(File video) {
        super.onVideoTaken(video);
    }
    
});
```

### Extra Attributes

```xml
<com.flurgle.camerakit.CameraView xmlns:camerakit="http://schemas.android.com/apk/res-auto"
    android:id="@+id/camera"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    camerakit:ckCropOutput="true"  
    camerakit:ckFacing="back"
    camerakit:ckFlash="off"
    camerakit:ckPictureMode="quality" />
```

#### `ckCropOutput`

| Value         | Description  |
| --------------| -------------|
| `true`        | Crop the output image or video to only contain what can be seen on the `CameraView` preview. |
| `false`       | Output the full image or video regardless of what is visible on the `CameraView` preview. |


#### `ckFacing`

| Value         | Description  |
| --------------| -------------|
| `back`        | Default `CameraView` preview to back camera. |
| `front`       | Default `CameraView` preview to front camera. |


#### `ckFlash`

| Value         | Description  |
| --------------| -------------|
| `off`         | Default `CameraView` flash to off. |
| `on`          | Default `CameraView` flash to on. |
| `auto`        | Default `CameraView` flash to automatic. |

#### `ckPictureMode`

| Value         | Description  |
| --------------| -------------|
| `quality`     | Use normal Android Camera API image capturing. |
| `speed`       | Freeze the `CameraView` preview and grab a `Bitmap` of the frame. |
| `auto`        | Default picture mode to `quality`, but fallback to `speed` if capturing is determined to be too slow. |

### Capturing Images

To capture an image just call `CameraView.capturePicture()`. Make sure you setup a `CameraListener` to handle the image callback.

```java
camera.setCameraListener(new CameraListener() {
    @Override
    public void onPictureTaken(byte[] picture) {
        super.onPictureTaken(picture);
        
        // Create a bitmap
        Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
    }
});

camera.takePicture();
```

### Capturing Video

To capture video just call `CameraView.startRecordingVideo()` to start, and `CameraView.stopRecordingVideo()` to finish. Make sure you setup a `CameraListener` to handle the video callback.

```java
camera.setCameraListener(new CameraListener() {
    @Override
    public void onVideoTaken(File video) {
        super.onVideoTaken(video);
        // The File parameter is an MP4 file. 
    }
});

camera.startRecordingVideo();
camera.postDelayed(new Runnable() {
    @Override
    public void run() {
        camera.stopRecordingVideo();
    }
}, 2500);
```

## Automatic Permissions Behavior

You can handle permissions yourself in whatever way you want, but if you make a call to `CameraView.start()` without the `android.permission.CAMERA` permission, an exception would normally be thrown and your app would crash.

With CameraKit, we will automatically prompt for the `android.permission.CAMERA` permission if it's not available. If you want to handle it yourself, just make sure you dont call `CameraView.start()` until you acquire the permissions.

[Insert GIF]

## Dynamic Sizing Behavior

Dynamic sizing behavior

## Capture Mode Behavior

Capture mode behavior

## Credits
Dylan McIntyre

## License
CameraKit-Android is [MIT licensed](https://github.com/wonderkiln/camerakit-android/blob/master/LICENSE).