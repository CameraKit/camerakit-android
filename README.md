<p align="center">
    <a href="https://camerakit.website" target="_blank">
        <img alt='CameraKit Header' src='.repo/gh-readme-header.png' />
    </a>
</p>

<p align="center">
    <a href="https://www.wonderkiln.com" target="_blank">
        <img alt='CameraKit Header' src='.repo/gh-readme-wk.png'/>
    </a>
    <a href="https://play.google.com/store/apps/details?id=com.wonderkiln.camerakit.demo&hl=en" target="_blank">
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

## Documentation
Check out our [Documentation Website](http://docs.camerakit.website) for more documentation on how to use to CameraKit.

# Sponsoring

Most of the core team members, CameraKit contributors and contributors in the ecosystem do this open source work in their free time. If you use CameraKit for a serious task, and you'd like us to invest more time on it, please donate. This project increases your income/productivity too. It makes development and applications faster and it reduces the required bandwidth.

This is how we use the donations:

- Allow the core team to work on CameraKit
- Thank contributors if they invested a large amount of time in contributing
- Support projects in the ecosystem that are of great value for users
- Infrastructure cost
- Fees for money handling

## Sponsors

[[Become a sponsor](https://opencollective.com/CameraKit-Android#sponsor)] and your logo will show up here with a link to your website.

<a href="https://www.expensify.com/"><img src=".repo/expensify-logo.png" width="150"></a>

<a href="https://opencollective.com/CameraKit-Android/sponsor/0/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/1/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/2/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/3/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/4/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/5/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/6/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/7/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/8/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/CameraKit-Android/sponsor/9/website" target="_blank"><img src="https://opencollective.com/CameraKit-Android/sponsor/9/avatar.svg"></a>

## Backers

[[Become a backer](https://opencollective.com/CameraKit-Android#backer)] and get your image on our README on Github with a link to your site.

<a href="https://opencollective.com/CameraKit-Android#backers" target="_blank"><img src="https://opencollective.com/CameraKit-Android/backers.svg?width=890"></a>

## Contributors

This project exists thanks to all the people who contribute. [[Contribute]](CONTRIBUTING.md).
<a href="graphs/contributors"><img src="https://opencollective.com/CameraKit-Android/contributors.svg?width=890" /></a>

---

## License
CameraKit is [MIT License](https://github.com/wonderkiln/CameraKit-Android/blob/master/LICENSE)
