<img alt='CameraKit Header' src='.repo/camerakit-android-header.png' height='125'/>

#####Originally a fork of [Google's CameraView library](https://github.com/google/cameraview).

CameraKit is an extraordinarily easy to use utility to work with the infamous Android Camera and Camera2 APIs. Built by [Dylan McIntyre](https://github.com/dwillmc).

Try out all the unique features using the CameraKit Demo from the Google Play store!

<a href='https://play.google.com/store/apps/details?id=com.flurgle.camerakit.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80'/></a>

<img src='.repo/demo1.png' width='120'/>
<img src='.repo/demo2.png' width='120'/>
<img src='.repo/demo3.png' width='120'/>
<img src='.repo/demo4.png' width='120'/>

## Table of Contents

- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
  - [Events](#events)
  - [Extra Attributes](#extra-attributes)
    - [`ckFacing`](#ckfacing)
    - [`ckFlash`](#ckflash)
    - [`ckFocus`](#ckfocus)
    - [`ckMethod`](#ckmethod)
    - [`ckZoom`](#ckzoom)
    - [`ckCropOutput`](#ckcropoutput)
    - [`ckJpegQuality`](#ckjpegquality)
  - [Capturing Images](#capturing-images)
  - [Capturing Video](#capturing-video)
- [Automatic Permissions Behavior](#automatic-permissions-behavior)
- [Dynamic Sizing Behavior](#dynamic-sizing-behavior)
  - [Output Cropping](#output-cropping)
  - [`adjustViewBounds`](#adjustviewbounds)
- [Capture Methods](#capture-methods)
  - [Standard](#standard)
  - [Still](#still)
  - [Auto](#auto)
- [Focus](#focus)
- [Credits](#credits)
- [License](#license)

## Features

- Image and video capture seamlessly working with the same preview session.
- Automatic use of both Camera and Camera2 APIs.
- Automatic system permission handling.
- Automatic preview scaling.
  - Create a `CameraView` of any size (not just presets!).
  - Automatic output cropping to match your `CameraView` bounds.
- Multiple capture methods.
  - `METHOD_STANDARD`: an image captured normally using the camera APIs.
  - `METHOD_STILL`: a freeze frame of the `CameraView` preview (similar to SnapChat and Instagram) for devices with slower cameras.
  - Automatic picture mode determination based on measured speed.
- Built-in tap to focus and auto focus.
- Built-in pinch to zoom.

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
    android:layout_height="wrap_content"
    android:adjustViewBounds="true" />
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
    android:layout_height="wrap_content"
    camerakit:ckFacing="back"
    camerakit:ckFlash="off"
    camerakit:ckFocus="continuous"
    camerakit:ckMethod="standard"
    camerakit:ckZoom="pinch"
    camerakit:ckCropOutput="true"  
    camerakit:ckJpegQuality="100"
    android:adjustViewBounds="true" />
```

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

#### `ckFocus`

| Value         | Description  |
| --------------| -------------|
| `off`         | Tap to focus is enabled and a visible focus circle appears when tapped, similar to the Android built-in camera. |
| `continuous`  | Tap to focus is enabled, but no focus circle appears. |
| `tap`         | Tap to focus is off. |

#### `ckMethod`

| Value         | Description  |
| --------------| -------------|
| `standard`    | Use normal Android Camera API image capturing. |
| `still`       | Freeze the `CameraView` preview and grab a `Bitmap` of the frame. |
| `auto` (coming soon) | Default picture mode to `standard`, but fallback to `still` if capturing is determined to be too slow. |

#### `ckZoom`

| Value         | Description  |
| --------------| -------------|
| `off`         | User can zoom using pinching gestures with their fingers.  |
| `pinch`       | User can zoom in and out using pinching gestures on the `CameraView`. |

#### `ckCropOutput`

| Value         | Description  |
| --------------| -------------|
| `true`        | Crop the output image or video to only contain what can be seen on the `CameraView` preview. |
| `false`       | Output the full image or video regardless of what is visible on the `CameraView` preview. |

#### `ckJpegQuality`

| Value         | Description  |
| --------------| -------------|
| `0 <= n <= 100`| Percent quality for returned JPEG data. |


### Capturing Images

To capture an image just call `CameraView.captureImage()`. Make sure you setup a `CameraListener` to handle the image callback.

```java
camera.setCameraListener(new CameraListener() {
    @Override
    public void onPictureTaken(byte[] picture) {
        super.onPictureTaken(picture);

        // Create a bitmap
        Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
    }
});

camera.captureImage();
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

With CameraKit, we will automatically prompt for the `android.permission.CAMERA` permission if it's not available. If you want to handle it yourself, just make sure you don't call `CameraView.start()` until you acquire the permissions.

![Permissions behavior gif](.repo/permissions.gif)

## Dynamic Sizing Behavior

You can setup the `CameraView` dimensions however you want. When your dimensions don't match the aspect ratio of the internal preview surface, the surface will be cropped minimally to fill the view. The behavior is the same as the `android:scaleType="centerCrop"` on an `ImageView`.

[Insert GIF]

### Output Cropping

When you capture output you can either capture everything - even what is not visible to the user on the `CameraView` - or just the visible preview. See [`ckCropOutput`](#ckcropoutput) above for usage.

[Insert GIF]

### `adjustViewBounds`

You can use a mix of a fixed dimension (a set value or `match_parent`) as well as `wrap_content`. When you do this make sure you set `android:adjustViewBounds="true"` on the `CameraView`.

When you do this the dimension set to `wrap_content` will automatically align with the true aspect ratio of the preview surface. In this case the whole preview will be visible with no cropping.

## Capture Methods

We decided to add multiple capture modes to CameraKit to allow you to give a better image capturing experience to users with slower cameras when appropriate.

See [`ckMethod`](#ckmethod) above for usage.

### Standard

When you use `METHOD_STANDARD` (`camerakit:ckMethod="standard"`), images will be captured using the normal camera API capture method using the shutter.

[Insert GIF]

### Still

When you use `METHOD_STILL` (`camerakit:ckMethod="still"`), images will be captured by grabbing a single frame from the preview. This behavior is the same as SnapChat and Instagram. This method has a higher rate of motion blur but can be a better experience for users with slower cameras.

[Insert GIF]

### Auto

When you use `METHOD_AUTO` (`camerakit:ckMethod="auto"`), images will be first be captured using the [standard](#standard) method. If capture consistently takes a long amount of time, the picture mode will fallback to [still](#still) capture.

[Insert GIF]

## Focus

Along with the always on auto-focus, you can enable tap to focus in your `CameraView`. See [`ckFocus`](#ckFocus) for usage. You have the option of having it on with a visible focus marker, on with no marker, or off.

[Insert GIF]

## Credits

Dylan McIntyre

## License

CameraKit-Android is [MIT licensed](https://github.com/wonderkiln/camerakit-android/blob/master/LICENSE).
