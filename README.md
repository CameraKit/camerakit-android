<p align="center">
    <a href="https://camerakit.io" target="_blank">
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
    <a href="https://spectrum.chat/camerakit/">
        <img src=".repo/gh-join-spectrum.svg" alt="Join Slack" height="28px">
    </a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android">
        <img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/coverage.svg" alt="Code Climate" style="margin-bottom: 4px;">
    </a>
    <a href="https://codeclimate.com/github/wonderkiln/CameraKit-Android">
        <img src="https://codeclimate.com/github/wonderkiln/CameraKit-Android/badges/issue_count.svg" alt="Code Climate" style="margin-bottom: 4px;">
    </a>
</p>

CameraKit takes one of the hardest Android APIs and makes it into a high level and easy to use library that solves all of your problems.

With CameraKit you are able to seamlessly do the following...

- Image and video capture seamlessly working with the same preview session.
- Automatic system permission handling.
- Automatic preview scaling.
  - Create a `CameraView` of any size (not just presets!).
  - Automatic output cropping to match your `CameraView` bounds.
- Multiple capture methods.
  - `METHOD_STANDARD`: an image captured normally using the camera APIs.
  - `METHOD_STILL`: a freeze frame of the `CameraView` preview (similar to SnapChat and Instagram) for devices with slower cameras.
  - `METHOD_SPEED`: automatic capture method determination based on measured speed.
- Built-in continuous focus.
- Built-in tap to focus.
- Built-in pinch to zoom.

## Sponsored By
<a href="https://www.infitting.com/"><img src=".repo/gh-brand-infitting-logo.svg" height="50px" style="margin: 20px"></a>
<a href="https://www.expensify.com/"><img src=".repo/gh-readme-expensify-logo.svg" height="50px" style="margin: 20px"></a>
<a href="https://www.goosechase.com/"><img src=".repo/gh-brand-goosechase-logo.svg" height="50px" style="margin: 20px"></a>
<a href="https://www.alpha-apps.ae/"><img src=".repo/gh-brand-alphaapps-logo.png" height="50px" style="margin: 20px; margin-bottom: 25px"></a>
<a href="https://www.buddy.works/"><img src=".repo/gh-readme-buddyworks.png" height="100px"></a>

# Pardon the dust
__CameraKit__ has been going through major changes in the past few months. We've slowed support on verison `0.13.2` and moved our focus to `1.0.0`. This release will bring improved stability, faster processing and a standard API for future development of __CameraKit__. The lastest version is currently in beta, `1.0.0-beta3.9`. 

The code lives on the branch `v1.0.0`. You can read the discussion about `1.0.0` on the pull request #318. If you have a question or want to talk directly with the team, leave us a message on [spectrum.chat](https://spectrum.chat/camerakit/).

The official `1.0.0` release is coming in a few short weeks. With it we will launch an all new documentation site. In the mean time check out the setup instructions for `1.0.0-beta3.9` below!


## Setup
To include __CameraKit__ in your project, add the following to your `app` level `build.gradle`.
```
repositories {
    maven { url 'https://dl.bintray.com/camerakit/camerakit-android' }
}

dependencies {
    implementation 'com.camerakit:camerakit:1.0.0-beta3.9'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.2.61'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:0.24.0'
}
```
## Usage
Create a `CameraKitView` in your layout as follows:
```
<com.camerakit.CameraKitView
    android:id="@+id/camera"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:adjustViewBounds="true"
    app:camera_facing="back"
    app:camera_focus="continuous"
    app:camera_permissions="camera" />
```

Then create a new `CameraKitView` object in your `Activity` and override the following methods.
```
private CameraKitView cameraKitView;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    cameraKitView = findViewById(R.id.camera);
}

@Override
protected void onStart() {
    super.onStart();
    cameraKitView.onStart();
}

@Override
protected void onResume() {
    super.onResume();
    cameraKitView.onResume();
}

@Override
protected void onPause() {
    cameraKitView.onPause();
    super.onPause();
}

@Override
protected void onStop() {
    cameraKitView.onStop();
    super.onStop();
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```
---
## License
CameraKit is [MIT License](https://github.com/CameraKit/CameraKit-Android/blob/master/LICENSE)
