The Twilio Programmable Video SDKs use [Semantic Versioning](http://www.semver.org).

####2.0.0-preview1

Features

- Added `EncodingParameters` which constrains how much bandwidth is used to share audio and 
video. This object has been added to `ConnectOptions` and can also be set on `LocalParticipant` 
after joining a `Room`.
- Added two static `create` methods to `LocalAudioTrack` and `LocalVideoTrack` that allow creating
named tracks. The following snippet demonstrates how to create a video track named "screen".

      LocalVideoTrack screenVideoTrack = LocalVideoTrack.create(context, 
              true, 
              screenCapturer, 
              "screen"); 
              
- Moved `getTrackId` from `Track` to `LocalAudioTrack` and `LocalVideoTrack`.
- Added `AudioCodec` and `VideoCodec` as part of the new codec preferences API. Audio and video
codec preferences can be set in `ConnectOptions`. The following snippet
demonstrates how to prefer the iSAC audio codec and VP9 video codec.

      ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
              .roomName(roomName)
              .preferAudioCodecs(Collections.singletonList(VideoCodec.ISAC))
              .preferVideoCodecs(Collections.singletonList(VideoCodec.VP9))
              .build();

- Added `RemoteAudioTrack` and `RemoteVideoTrack`. These new objects extend `AudioTrack` and 
`VideoTrack` respectively and come with the following new method: 
  - `getName` - Returns the name of the track or an empty string if no name is specified.
- Added `enablePlayback` to new `RemoteAudioTrack` which allows developers to mute the audio 
 received from a `RemoteParticipant`.
- Added `RemoteAudioTrackPublication` which represents a published `RemoteAudioTrack`. This new 
class contains the following methods: 
  - `getTrackSid` - Returns the identifier of a remote video track within the scope of a `Room`.
  - `getTrackName` - Returns the name of the track or an empty string if no name was specified.
  - `isTrackEnabled` - Checks if the track is enabled.
  - `getAudioTrack` - Returns the base class object of the remote audio track published.
  - `getRemoteAudioTrack` - Returns the remote audio track published.
- Added `RemoteVideoTrackPublication` which represents a published `RemoteVideoTrack`. This new 
class contains the following methods: 
  - `getTrackSid` - Returns the identifier of a remote video track within the scope of a `Room`.
  - `getTrackName` - Returns the name of the track or an empty string if no name was specified.
  - `isTrackEnabled` - Checks if the track is enabled.
  - `getAudioTrack` - Returns the base class object of the remote audio track published.
  - `getRemoteAudioTrack` - Returns the remote audio track published.
- Added `LocalAudioTrackPublication` which represents a published `LocalAudioTrack`. This new 
class contains the following methods: 
  - `getTrackSid` - Returns the identifier of a local video track within the scope of a `Room`.
  - `getTrackName` - Returns the name of the track or an empty string if no name was specified.
  - `isTrackEnabled` - Checks if the track is enabled.
  - `getAudioTrack` - Returns the base class object of the local audio track published.
  - `getLocalAudioTrack` - Returns the local audio track published.
- Added `LocalVideoTrackPublication` which represents a published `LocalVideoTrack`. This new 
class contains the following methods: 
  - `getTrackSid` - Returns the identifier of a local video track within the scope of a `Room`.
  - `getTrackName` - Returns the name of the track or an empty string if no name was specified.
  - `isTrackEnabled` - Checks if the track is enabled.
  - `getAudioTrack` - Returns the base class object of the local audio track published.
  - `getLocalAudioTrack` - Returns the local audio track published.
- Converted `Participant` to an interface and migrated previous functionality into 
`RemoteParticipant`. `LocalParticipant` and the new `RemoteParticipant` implement `Participant`.
- Added `RemoteParticipant#getRemoteAudioTracks` and `RemoteParticipant#getRemoteVideoTracks` which
return `List<RemoteAudioTrackPublication>` and `List<RemoteVideoTrackPublication>` respectively.
- Moved `Participant.Listener` to `RemoteParticipant.Listener` and changed the listener to return
`RemoteParticipant`, `RemoteAudioTrackPublication`, and `RemoteVideoTrackPublication` in callbacks.
- Renamed the following `RemoteParticipant.Listener` callbacks:
  - `onAudioTrackAdded` renamed to `onAudioTrackPublished`.
  - `onAudioTrackRemoved` renamed to `onAudioTrackUnpublished`.
  - `onVideoTrackAdded` renamed to `onVideoTrackPublished`.
  - `onVideoTrackRemoved` renamed to `onVideoTrackUnpublished`.
- Added the following callbacks to `RemoteParticipant.Listener`:
  - `onAudioTrackSubscribed` - Indicates when audio is flowing from a remote particpant's audio 
  track. This callback includes the `RemoteAudioTrack` that was subscribed to.
  - `onAudioTrackUnsubscribed` - Indicates when audio is no longer flowing from a remote 
  partipant's audio track. This callback includes the `RemoteAudioTrack` that was subscribed to.
  - `onVideoTrackSubscribed` - Indicates when video is flowing from a remote participant's video 
  track. This callback includes the `RemoteVideoTrack` that was subscribed to.
  - `onVideoTrackUnsubscribed` - Inidicates when video is no longer flowing from a remote
  participant's video track. This callback includes the `RemoteVideoTrack` that was subscribed to.
- Renamed `TrackStats` to `RemoteTrackStats`, `AudioTrackStats` to `RemoteAudioTrackStats`, and
`VideoTrackStats` to `RemoteVideoTrackStats`
- Renamed `LocalParticipant#addAudioTrack` and `LocalParticipant#addVideoTrack` to 
`LocalParticipant#publishedTrack`.
- Added `LocalParticipant.Listener` which is provides the following callbacks:
  - `onAudioTrackPublished` - Indicates when a local audio track has been published to a `Room`.
  - `onVideoTrackPublished` - Indicates when a local video track has been published to a `Room`.
- Added `LocalParticipant#getLocalAudioTracks` and `LocalParticipant#getLocalVideoTracks` which
return `List<LocalAudioTrackPublication>` and `List<LocalVideoTrackPublication>` respectively.

Improvements

- Null renderers cannot be added or removed from local or remote video tracks.
- Renderers cannot be added or removed from a `LocalVideoTrack` that has been released. 

Bug Fixes

- Change visibility of `LocalParticipant#release()` from public to package. 
[#132](https://github.com/twilio/video-quickstart-android/issues/132)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.3.0

Features
- Added static method `CameraCapturer.isSourceAvailable` that validates if a camera source is 
available on the device. This method is used when creating a `CameraCapturer` instance and when
calling `CameraCapturer#switchCamera` to validate that a source can be used for capturing frames.

Improvements

- Added javadoc to `Participant.Listener`, `ScreenCapturer.Listener`, `VideoCapturer.Listener`, 
and `VideoRenderer.Listener`. 

Bug Fixes

- Fixed a bug where multiple participants adding/removing tracks at the same time was not handled 
properly.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.2.2

Improvements

- Calling `Participant#setListener` with `null` is no longer allowed. 

Bug Fixes

- Removed reference to `LocalMedia` in `CameraCapturer` javadoc.
- Fixed race condition that could result in track events not being raised.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.2.1

Improvements

- Improved safety of asynchronous operations in native core.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.2.0

Features

- The SDK now uses TLS 1.2 in favor of TLS 1.0 to connect to Twilio’s servers.

Improvements

- Deprecated `LocalParticipant#release`. This method is not meant to be called and is now a 
no-op until it is removed in `2.0.0-preview1` release. [#132](https://github.com/twilio/video-quickstart-android/issues/132)
- Added more checks and logging to `CameraCapturer` to help identify cases when the camera service cannot be reached. [#126](https://github.com/twilio/video-quickstart-android/issues/126)
- Changed `getSupportedFormats` for `CameraCapturer`, `ScreenCapturer`, and `Camera2Capturer` to 
be `synchronized`.

Bug Fixes

- Fixed timing issue where camera was not always available after a video track was released. [#126](https://github.com/twilio/video-quickstart-android/issues/126)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.1.1

Bug Fixes

- Fixed bug in `VideoConstraints` logic where valid VideoCapturer video formats were ignored due to very strict checking of aspect ratios in WebRTC
- Fixed bug in Logger.java where setting certain LogLevel's did not print error logs 
- Fixed bug in `LocalVideoTrack` where FPS check was incorrectly marking a constraint as incompatible. [#127](https://github.com/twilio/video-quickstart-android/issues/127)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.1.0

Improvements

- Moved signaling network traffic to port 443. 
- Added `Camera2Capturer`. `Camera2Capturer` uses `android.hardware.camera2` to implement 
a `VideoCapturer`. `Camera2Capturer` does not yet implement `takePicture` and the ability to modify 
camera parameters once `Camera2Capturer` is running.

Create `LocalVideoTrack` with `Camera2Capturer`

    
    // Check if device supports Camera2Capturer 
    if (Camera2Capturer.isSupported(context)) {
        // Use CameraManager.getCameraIdList() for a list of all available camera IDs
        String cameraId = "0";
        Camera2Capturer.Listener camera2Listener = new Camera2Capturer.Listener() {
                @Override
                public void onFirstFrameAvailable() {}

                @Override
                public void onCameraSwitched(String newCameraId) {}

                @Override
                public void onError(Camera2Capturer.Exception exception) {}
        }
        Camera2Capturer camera2Capturer = new Camera2Capturer(context, cameraId, camera2Listener);
        LocalVideoTrack = LocalVideoTrack.create(context, true, camera2Capturer);
    }
    
- This release adds Insights statistics collection, which reports RTP quality metrics back to 
Twilio. In the future, these statistics will be included in customer-facing reports visible in the 
Twilio Console. Insights collection is enabled by default, if you wish to disable it reference
the following snippet.


    ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .enableInsights(false)
            .build();

Bug Fixes

- Improved signaling connection retry logic. In the case of an error, the SDK will continue 
to retry with a backoff timer when errors are encountered.
- Fixed a bug in network handoff scenarios where the SDK was not handling the race condition 
if network lost or network changed event is received when a network changed event is being processed.
- Fixed bug where audio and video tracks were not available after `onParticipantDisconnected` was
invoked [#125](https://github.com/twilio/video-quickstart-android/issues/125)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.0.2

Bug Fixes

- Backported fix for Chromium bug [679306](https://codereview.webrtc.org/2879073002).

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.0.1

Improvements

- Improved internal native Room operations.
- Improved `ScreenCapturer` performance by enabling capturing to a texture.
- Added new error code for case when participant gets disconnected because of duplicate participant 
joined the room. 

Bug Fixes

- Fixed issue adding audio/video tracks quickly while connected to a room [#90](https://github.com/twilio/video-quickstart-android/issues/90)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.0.0
We've promoted 1.0.0-beta17 to 1.0.0 as our first General Availability release.

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- Disconnecting from a `Room` that has not connected sometimes results in a crash [#116](https://github.com/twilio/video-quickstart-android/issues/116) 
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

####1.0.0-beta17

Improvements

- Replaced `LocalMedia` class with Track factories for `LocalVideoTrack` and `LocalAudioTrack`

Working with `LocalVideoTrack` and `LocalAudioTrack` before 1.0.0-beta17

    // Create LocalMedia
    LocalMedia localMedia = LocalMedia.create(context);
    LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, videoCapturer);
    LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);

    ...

    // Destroy LocalMedia to free native memory resources
    localMedia.release();

Working with `LocalVideoTrack` and `LocalAudioTrack` now

    // Create Tracks
    LocalVideoTrack localVideoTrack = LocalVideoTrack.create(context, true, videoCapturer);
    LocalAudioTrack localAudioTrack = LocalAudioTrack.create(context, true);

    ...

    // Destroy Tracks to free native memory resources
    localVideoTrack.release();
    localAudioTrack.release();

- The `ConnectOptions.Builder` now takes a `List<LocalAudioTrack>` and `List<LocalVideoTrack>` instead of `LocalMedia`

Providing `LocalVideoTrack` and `LocalAudioTrack` before 1.0.0-beta17

    LocalMedia localMedia = LocalMedia.create(context);
    LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, videoCapturer);
    LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);

    ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
        .roomName(roomName)
        .localMedia(localMedia)
        .build();
    VideoClient.connect(context, connectOptions, roomListener);

Providing `LocalVideoTrack` and `LocalAudioTrack` now

    List<LocalVideoTrack> localAudioTracks =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};
    List<LocalAudioTrack> localVideoTracks =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};

    ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
        .roomName(roomName)
        .audioTracks(localAudioTracks)
        .videoTracks(localVideoTracks)
        .build();
    VideoClient.connect(context, connectOptions, roomListener);
- Methods `getVideoTracks()` and `getAudioTracks()` moved from `LocalMedia` and `Media` to `LocalParticipant` and `Participant`
- Removed `Media` from `Participant` and migrated `Media.Listener` to `Participant.Listener`.
`AudioTrack` and `VideoTrack` events are raised with the corresponding `Participant` instance.
This allows you to create tracks while connected to a `Room` without immediately adding them to the connected `Room`
- Improved hardware accelerated decoding through the use of surface textures.
- Added `textureId` and `samplingMatrix` fields to `I420Frame` so implementations of `VideoRenderer`
can extract YUV data from frame represented as texture.
- Exposed `org.webrtc.YuvConverter` to facilitate converting a texture to an in memory YUV buffer.
- Invoke `ScreenCapturer.Listener` callbacks on the thread `ScreenCapturer` is created on.
- Fixed an issue where the ConnectivityReceiver was causing a reconnect when connecting to a `Room` for the first time occasionally leading to a 53001 error `onConnectFailure` response.
- `Room#getParticipants` returns `List<Participant>` instead of `Map<String, Participant>`.

Bug Fixes

- On Nexus 9 device, intermittent high decoding times results in delayed video. [#95](https://github.com/twilio/video-quickstart-android/issues/95)
- Unsatisfied link errors for `org.webrtc.voiceengine.WebRtcAudioManager` and `org.webrtc.voiceengine.WebRtcAudioTrack` construction. [#102](https://github.com/twilio/video-quickstart-android/issues/102)
- VideoTrack isEnabled() returning wrong state [#104](https://github.com/twilio/video-quickstart-android/issues/104)

Known issues

- Network handoff, and subsequent connection renegotiation is not supported for IPv6 networks [#72](https://github.com/twilio/video-quickstart-android/issues/72)
- VP8 is the only supported codec [#71](https://github.com/twilio/video-quickstart-android/issues/71)
- Participant disconnect event can take up to 120 seconds to occur [#80](https://github.com/twilio/video-quickstart-android/issues/80) [#73](https://github.com/twilio/video-quickstart-android/issues/73)
- Missing media when adding audio/video tracks quickly while connected to room [#90](https://github.com/twilio/video-quickstart-android/issues/90)
- LocalParticipant release method is public [#132](https://github.com/twilio/video-quickstart-android/issues/132)

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
- Unsatisfied link errors for `org.webrtc.voiceengine.WebRtcAudioManager` and `org.webrtc.voiceengine.WebRtcAudioTrack` construction. [#102](https://github.com/twilio/video-quickstart-android/issues/102)
- VideoTrack isEnabled() returning wrong state [#104](https://github.com/twilio/video-quickstart-android/issues/104)

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

