The Twilio Programmable Video SDKs use [Semantic Versioning](http://www.semver.org).

####1.0.0-beta16

Improvements

- Added enum `VideoFrame.RotationAngle` to ensure `VideoFrame` objects are constructed with
valid orientation values.
- Updated `CameraCapturer` to be powered by latest WebRTC camera capturer.
- Updated `CameraCapturer` to allow scheduling a picture to be taken while the capturer is not 
running.

Bug Fixes

- Reverted decoding from surface textures. This revert 
should fix problems for custom `VideoRenderer`s receiving `null` YUV data for `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- On Nexus 9 device, intermittent high decoding times results in delayed video. [#95](https://github.com/twilio/video-quickstart-android/issues/95)

####1.0.0-beta15

Improvements

- Upgraded to WebRTC 57.
- Renaming `VideoClient` class to `Video`.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta14

Improvements

- Simplified internal data structures that populate `StatsReport`.

Bug Fixes

- Fixed teardown crash that occurred in component that fetches ice servers.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta13

Improvements

- Decreased Room connection time by establishing the signaling connection earlier in the process.
- Removed the final case where we resolve localhost. This also improves connection time to your first Room.

Bug Fixes

- Fixed a regression in 1.0.0-beta12 where a track added event was not raised when the trackId was reused. [#83](https://github.com/twilio/video-quickstart-android/issues/83)
- Fixed crash in `Room#disconnect` when releasing `Participant` media
- Resolved memory corruption issues which could occur in multi-party scenarios.
- Fixed a crash which could occur in signaling stack

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta12

Improvements

- Made `VideoClient` an abstract class.
- We've begun formalizing our error codes. They are divided up into Signaling (530xx), Room (531xx), Participant (532xx), Track (533xx), Media (534xx), Configuration (535xx), and Access Token (201xx) subranges. Instances of `TwilioException` will now carry a numeric code belonging to one of these ranges, an error message, and an optional error explanation.
- Implemented a policy for applying `VideoConstraints`. Adding a `LocalVideoTrack` with no constraints, results in `LocalMedia` applying a set of default constraints based on the closest supported `VideoFormat` to 640x480 at 30 FPS. Adding a `LocalVideoTrack` with custom constraints, results in `LocalMedia` checking if the constraints are compatible with the given `VideoCapturer` before applying. If the constraints are not compatible `LocalMedia` applies default constraints. [#68](https://github.com/twilio/video-quickstart-android/issues/68)

Bug Fixes

- Fixed echo cancellation bug for Nexus 6P [#65](https://github.com/twilio/video-quickstart-android/issues/65).

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta11

Improvements

- Moved `connect` from instance method to static method on `VideoClient` class. Calling the new static `connect` method requires a `Context` in addition to `ConnectOptions` and a `Room.Listener` . `VideoClient` is no longer an object that can be instantiated and an instance is no longer required to connect to a `Room`.
- Moved access token parameter from `VideoClient` constructor to `ConnectOptions.Builder` constructor. 

Connecting to a `Room` before 1.0.0-beta11

    // Create VideoClient
    VideoClient videoClient = new VideoClient(context, accessToken);
    ConnectOptions connectOptions = new ConnectOptions.Builder()
        .roomName(roomName)
        .localMedia(localMedia)
        .build();
    videoClient.connect(connectOptions, roomListener);

Connecting to a `Room` with static `connect` 

    ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
        .roomName(roomName)
        .localMedia(localMedia)
        .build();
    VideoClient.connect(context, connectOptions, roomListener);

Bug Fixes

- Fixed crash when disconnecting from a `Room` on HTC 10.
- Fixed crash caused by removing a track before calling `Room#disconnect` .
- Use a certificate bundle to validate SSL certificates on the signaling connection.
- Improved compatibility with Group Rooms and track added and removed events.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta10

Improvements

- Network handoff, and subsequent connection renegotiation is now supported for IPv4 networks.

Bug Fixes

- Fixed a regression introduced in 1.0.0-beta8 where tokens with purely numeric identities caused a crash [#64](https://github.com/twilio/video-quickstart-android/issues/64) [#60](https://github.com/twilio/video-quickstart-android/issues/60)
- Participant identities now support UTF-8

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta9

Bug Fixes

- Fixed immediate disconnect when using custom ICE servers

Known issues

- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Tokens with purely numeric identities results in a crash
- Participant identities with unicode characters are not supported
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta8

Features

- Added an `isRecording` method to `Room`, and callbacks to `RoomListener`. Please note that recording is only available in our Group Rooms developer preview. `isRecording` will always return `false` in a P2P Room.

Bug Fixes

- Fixed camera freeze when using `CameraCapturer#updateCameraParameters`  API #54
- Fixed crash when caused by calling `getStats()`  immediately after disconnecting from `Room` 
- Fixed heap corruption on HTC 10
- Fixed memory leaks parsing signaling messages
- Attempt ICE restarts when a PeerConnection fails

Known issues

- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Tokens with purely numeric identities results in a crash
- Participant identities with unicode characters are not supported
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta7

Improvements

- Clarified documentation for `LocalMedia#addAudioTrack`  enabled parameter

Bug Fixes

- Fixed crash loading library on some devices. [#53](https://github.com/twilio/video-quickstart-android/issues/53)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Disconnecting from a `Room` immediately after calling `getStats()` results in a crash.
- Participant identities with unicode characters are not supported
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)

####1.0.0-beta6

Bug Fixes

- Fixed black frames being rendered after device is rotated.
- Fixed crash in `EglBaseProvider`. 

Known issues

- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Disconnecting from a `Room` immediately after calling `getStats()` results in a crash.
- Native library fails to load on some devices. [#53](https://github.com/twilio/video-quickstart-android/issues/53)
- Participant identities with unicode characters are not supported
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)


####1.0.0-beta5

New features

- Upgraded to WebRTC 55.
- Upgraded to NDK r12b.
- Improved [quickstart](https://github.com/twilio/video-quickstart-android) README.
- Added a `getStats()` method to `Room` that builds a `StatsReport` with metrics for all the audio and video tracks being shared to a `Room`.
- Improved hardware accelerated decoding through the use of surface textures.
- Standardized error messages and codes for a `Room`.
- Changed `VideoException` to `TwilioException`.

Bug Fixes

- Fixed picture orientation bug for `takePicture`.
- `PictureListener` callbacks are invoked on the calling thread of `takePicture`.
- Reduced previously high decoding times for Nexus 9.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Disconnecting from a `Room` immediately after calling `getStats()` results in a crash.
- Participant identities with unicode characters are not supported
- Missing YUV data when adding a custom `VideoRenderer` to `VideoTrack`s [#93](https://github.com/twilio/video-quickstart-android/issues/93)


####1.0.0-beta4

New features

- Added new API to `CameraCapturer` for taking a picture.
- Quickstart has been updated to demonstrate the use of APK splits to reduce APK size.

Known issues

- IPv6 is not fully supported.
- Network handoff, and subsequent connection renegotiation is not supported.
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- On Nexus 9 device, intermittent high decoding times results in delayed video.
- Participant identities with unicode characters are not supported

####1.0.0-beta3

New features

- Added new API to `CameraCapturer` for providing custom `Camera.Parameters`.
- Added `isScreencast()` method to `VideoCapturer`. This indicates if a capturer is providing screen content and affects any scaling attempts made while media is flowing.

Bug fixes

- Fixed crashes when RECORD_AUDIO and CAMERA permission are not granted. `LocalMedia` will now return `null` when attempting to add a `LocalAudioTrack` without RECORD_AUDIO permission. `CameraCapturer` will log an error and provide an error code via a new `CameraCapturer.Listener` when trying to capture video without CAMERA permission.

Known issues

- IPv6 is not fully supported
- Network handoff, and subsequent connection renegotiation is not supported
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant identities with unicode characters are not supported

####1.0.0-beta2

New features

- Removed dependency on `AccessManager` in `VideoClient` constructor. Only a context and access token are required to create a `VideoClient`.
- Added an `updateToken` method to `VideoClient` that allows for an access token to be updated in case it has expired

Bug fixes

- Fixed crashes on x86 and x86_64 devices

Known issues

- IPv6 is not fully supported
- Network handoff, and subsequent connection renegotiation is not supported
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant identities with unicode characters are not supported

####1.0.0-beta1

New features

- Added preliminary support for network handover while connected to a Room

Bug fixes

- Provide a `release()` method on the `I420Frame`  object allowing developers to free native memory once they are done using the frame when implementing their own custom renderers

Known issues

- IPv6 is not fully supported
- Network handoff, and subsequent connection renegotiation is not supported
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Using x86 or x86_64 devices results in a crash
- Participant identities with unicode characters are not supported

####1.0.0-preview2

New features

- Added support for AudioOptions
- Upgraded to Android Nougat from Marshmallow

Known issues

- IPv6 is not fully supported
- Network handoff, and subsequent connection renegotiation is not supported
- Resource leak when implementing custom `VideoRenderer`
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Using x86 or x86_64 devices results in a crash
- Participant identities with unicode characters are not supported

####1.0.0-preview1

New features

- Adopting Room based communications model. The invite model has been completely removed

Bug fixes

- First developer preview release

Known issues

- IPv6 is not fully supported
- Network handoff, and subsequent connection renegotiation is not supported
- Resource leak when implementing custom `VideoRenderer` 
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Using x86 or x86_64 devices results in a crash
- Participant identities with unicode characters are not supported

**Looking for older changlog entries?** [You can find the changelog for the deprecated Programmable Video Conversations API here](https://www.twilio.com/docs/api/video/conversations-api-deprecated/changelogs/android).

