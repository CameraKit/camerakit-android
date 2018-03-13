<p align="center">
    <a href="https://camerakit.website" target="_blank">
        <img alt='CameraKit Header' src='.repo/gh-readme-header.png' />
    </a>
</p>

<p align="center">
    <a href="https://www.wonderkiln.com" target="_blank">
        <img alt='WonderKiln Promo' src='.repo/gh-readme-wk.png'/>
    </a>
    <a href="https://play.google.com/store/apps/details?id=com.camerakit.demo&hl=en" target="_blank">
        <img alt='CameraKit Header' src='.repo/gh-readme-app.png'/>
    </a>
</p>

<p align="center">
    <a href="https://join-slack.camerakit.website"><img src="https://join-slack.camerakit.website/badge.svg" alt="Build Status"></a>
    <a href="https://circleci.com/gh/CameraKit/camerakit-android/tree/master"><img src="https://circleci.com/gh/CameraKit/camerakit-android/tree/master.svg?style=shield" alt="CircleCI"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/gpa.svg" alt="Code Climate"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/coverage.svg" alt="Code Climate"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/issue_count.svg" alt="Code Climate"></a>
</p>

CameraKit takes one of the hardest Android APIs and makes it into a high level and easy to use library that solves all of your problems.

## Setup
Add __CameraKit__ to the dependencies block in your `app` level `build.gradle`:

```groovy
implementation 'com.camerakit:camerakit:1.0.0'
```


## Usage

To use CameraKit, simply add a `CameraView` to your layout:

```xml
<com.camerakit.CameraView
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

## Detailed Documentation
To check out detailed docs, visit our [Documentation Website](http://docs.camerakit.website)

## Sponsored by Expensify
<a href="https://www.expensify.com/"><img src=".repo/expensify-logo.png" width="250"></a>

## License
CameraKit is [MIT License](https://github.com/wonderkiln/CameraKit-Android/blob/master/LICENSE)