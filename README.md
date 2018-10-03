<p align="center">
    <a href="https://camerakit.website" target="_blank">
        <img alt='CameraKit Header' src='.repo/gh-readme-header.svg' />
    </a>
</p>

<p align="center">
    <a href="https://play.google.com/store/apps/details?id=com.camerakit.demo&hl=en" target="_blank">
        <img alt='CameraKit Header' height="42px" src='.repo/gh-readme-app.svg'/>
    </a>
    <a href="https://buddy.works/" target="_blank">
        <img alt='Buddy.Works' height="42px" src='https://assets.buddy.works/automated-dark.svg'/>
    </a>
</p>

<p align="center">
    <a href="https://join-slack.camerakit.website"><img src="https://join-slack.camerakit.website/badge.svg" alt="Join Slack"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/coverage.svg" alt="Code Climate"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/issue_count.svg" alt="Code Climate"></a>
</p>

# What is CameraKit?

CameraKit takes one of the hardest Android APIs and makes it into a high level and easy to use library that solves all of your problems.

With CameraKit you are able to effortlessly do the following...

- Image and video capture seamlessly working with the same preview session.
- Automatic system permission handling.
- Automatic preview scaling.
- Create a CameraView of any size (not just presets!).
- Automatic output cropping to match your CameraView bounds.
- Multiple capture methods.
  - METHOD_STANDARD: an image captured normally using the camera APIs.
  - METHOD_STILL: a freeze frame of the CameraView preview (similar to SnapChat and Instagram) for devices with slower cameras.
  - METHOD_SPEED: automatic capture method determination based on measured speed.
- Built-in continuous focus.
- Built-in tap to focus.
- Built-in pinch to zoom.

## Sponsored By

<a href="https://www.expensify.com/"><img src=".repo/gh-readme-expensify.png"></a>
<a href="https://www.buddy.works/"><img src=".repo/gh-readme-buddyworks.png"></a>

## Setup

Add __CameraKit__ to the dependencies block in your `app` level `build.gradle`:

```groovy
implementation 'com.camerakit:camerakit:1.0.0-beta3.9'
```


## Usage

To use CameraKit, simply add a `CameraKitView` to your layout:

```xml
<com.camerakit.CameraKitView
    android:id="@+id/camera"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Make sure you override `onResume` and `onPause` in your activity, and make pass-through calls CameraKit with `CameraKitView.onResume()` and `CameraKitView.onPause()`.

```java
@Override
protected void onResume() {
    super.onResume();
    cameraView.onResume();
}

@Override
protected void onPause() {
    cameraView.onPause();
    super.onPause();
}
```

## Detailed Documentation

To check out detailed docs, visit our [Documentation Website](https://docs.camerakit.website)

## License

CameraKit is [MIT License](https://github.com/CameraKit/CameraKit-Android/blob/master/LICENSE)