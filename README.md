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
    - [`ckTapToFocus`](#cktaptofocus)
  - [Capturing Images](#capturing-images)
  - [Capturing Video](#capturing-video)
- [Automatic Permissions Behavior](#automatic-permissions-behavior)
- [Dynamic Sizing Behavior](#dynamic-sizing-behavior)
  - [Output Cropping](#output-cropping)
  - [`adjustViewBounds`](#adjustviewbounds)
- [Capture Mode Behavior](#capture-mode-behavior)
  - [Quality](#quality)
  - [Speed](#speed)
  - [Auto](#auto)
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
- Built-in tap to focus and auto focus.

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
    camerakit:ckPictureMode="quality"
    camerakit:ckTapToFocus="on" />
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
| `on`          | Tap to focus is enabled and a visible focus circle appears when tapped, similar to the Android built-in camera. |
| `invisible`   | Tap to focus is enabled, but no focus circle appears. |
| `off`         | Tap to focus is off. |

#### `ckTapToFocus`

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

![Permissions behavior gif](.repo/permissions.gif)

## Dynamic Sizing Behavior

You can setup the `CameraView` dimensions however you want. When your dimensions don't match the aspect ratio of the internal preview surface, the surface will be cropped minimally to fill the view. The behavior is the same as the `android:scaleType="centerCrop"` on an `ImageView`.

[Insert GIF]

### Output Cropping

When you capture output you can either capture everything - even what is not visible to the user on the `CameraView` - or just the visible preview. See [`ckCropOutput`](#ckcropoutput) above for usage.

[Insert GIF]

### `adjustViewBounds`

You can use a mix of a fixed dimension (a set value or `match_parent`) as well as `wrap_content`. When you do this make sure you set `android:adjustViewBounds="true"` on the `CameraView`.

When you do this the dimension set to `wrap_content` will automatically align with the true aspect ratio of the preview surface. In this case the whole preview will be visible.

## Capture Mode Behavior

We decided to add multiple capture modes to CameraKit to allow you to give a better image capturing experience to users with slower cameras when appropriate.

See [`ckPictureMode`](#ckpicturemode) above for usage.

### Quality

When you use `PICTURE_MODE_QUALITY` (`camerakit:ckPictureMode="quality"`), images will be captured using the normal camera API capture method using the shutter.

[Insert GIF]

### Speed

When you use `PICTURE_MODE_SPEED` (`camerakit:ckPictureMode="speed"`), images will be captured by grabbing a single frame from the preview. This behavior is the same as SnapChat and Instagram. This method has a higher rate of motion blur but can be a better experience for users with slower cameras.

[Insert GIF]

### Auto

When you use `PICTURE_MODE_AUTO` (`camerakit:ckPictureMode="speed"`), images will be first be captured using the [quality](#quality) method. If capture consistently takes a long amount of time, the picture mode will fallback to [speed](#speed).

[Insert GIF]

## Tap To Focus

Along with the always on auto-focus, you can enable tap to focus in your `CameraView`. See [`ckTapToFocus`](#cktaptofocus) for usage. You have the option of having it on with a visible focus marker, on with no marker, or off.

[Insert GIF]

## Credits

Dylan McIntyre

## License

CameraKit-Android is [MIT licensed](https://github.com/wonderkiln/camerakit-android/blob/master/LICENSE).