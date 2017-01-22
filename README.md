![CameraKit Header](.repo/camerakit-android-header.png)

CameraKit is an extraordinarily easy to use utility to work with the infamous Android Camera and Camera2 APIs. Built by [Dylan McIntyre](https://github.com/dwillmc).


Try out all the unique features using the CameraKit Demo from the Google Play store!

<a href='https://play.google.com/store/apps/details?id=com.flurgle.camerakit.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height='80'/></a>


## Table of Contents
- [Features](#features)
- [Setup](#setup)
- [Usage](#usage)
  - [Extra Attributes](#extra-attributes)
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

### Extra Attributes

Extra attributes

### Capturing images

Capturing photos

### Capturing Video

Capturing video

## Automatic Permissions Behavior

Automatic permissions behavior

## Dynamic Sizing Behavior

Dynamic sizing behavior

## Capture Mode Behavior

Capture mode behavior

## Credits
Dylan McIntyre

## License
CameraKit-Android is [MIT licensed](https://github.com/wonderkiln/camerakit-android/blob/master/LICENSE).