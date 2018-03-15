# CameraKit Testing Procedures

For each release we plan to run all code changes through a specific flow and test regiment, on varying devicews. THe report of these tests will be reported via a comment or in the PR Header ahead of any and all merges.

## Tests
### Test Single Photo (Portrait)

- Open CameraKit
  - Take single photo
    - Verify photo is created and saved as Portrait Photo
    - Verify photo is in-focus, and error free

### Test Single Photo (Landscape)

- Open CameraKit
  - Take single photo
    - Verify photo is created and saved as Landscape Photo
    - Verify photo is in-focus, and error free

### Engage Camera Controls and Take Photo (Portrait)

- Open CameraKit
  - Change Orientation to 90, 180, 270, 0
    - Confirm Preview Updates properly for each orietantation
  - Tap to Focus and Take Photo
    - Confirm preview focuses correctly
    - COnfirm saved photo matches preview
  - Pinch to Zoom (In + Out) and Take Photo
    - Confirm preview zooms correctly
    - COnfirm saved photo matches preview
  - Take single photo
    - Verify photo is created and saved as Landscape Photo
    - Verify photo is in-focus, and error free

### Test Camera under Rapid Fire

- Open CameraKit
  - Take succesive photos (30 within 30s)
    - Verify all 30 photos are created and saved
    - Verify each photo appears without error


## Devices being tested on
- Essential Phone
- Google Pixel
- Samsung Galaxy S8

Want to request a device? Make a issue in Github! =)
