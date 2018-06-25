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
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/coverage.svg" alt="Code Climate"></a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android"><img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/issue_count.svg" alt="Code Climate"></a>
</p>

CameraKit takes one of the hardest Android APIs and makes it into a high level and easy to use library that solves all of your problems.

## Supported by these Awesome Services
[<figure><img src="https://buddy.works" /><figcaption>Automated by Buddy](https://assets.buddy.works/automated-white.svg)</figcaption></figure>

## Sponsors
<a href="https://www.expensify.com/"><img src=".repo/gh-readme-expensify.png"></a>
<a href="https://www.buddy.works/"><img src=".repo/gh-readme-buddyworks.png"></a>

## Setup
Add __CameraKit__ to the dependencies block in your `app` level `build.gradle`:

```groovy
implementation 'com.camerakit:camerakit:1.0.0'
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
To check out detailed docs, visit our [Documentation Website](http://docs.camerakit.website)

## License
CameraKit is [MIT License](https://github.com/CameraKit/CameraKit-Android/blob/master/LICENSE)