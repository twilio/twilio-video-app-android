package com.twilio.video.app.sdk

import android.content.Context
import android.content.SharedPreferences
import com.twilio.video.CameraCapturer
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoConstraints
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.ASPECT_RATIO
import com.twilio.video.app.data.Preferences.ASPECT_RATIOS
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoTrackUpdated
import com.twilio.video.app.util.CameraCapturerCompat
import timber.log.Timber
import java.lang.RuntimeException

class LocalParticipantManager(
        private val context: Context,
        private val roomManager: RoomManager,
        private val sharedPreferences: SharedPreferences
) {

    private var localAudioTrack: LocalAudioTrack? = null
    private var localParticipant: LocalParticipant? = null
    private var cameraVideoTrack: LocalVideoTrack? = null
        set(value) {
            field = value
            roomManager.sendRoomEvent(VideoTrackUpdated(value))
        }
    private val cameraCapturer: CameraCapturerCompat by lazy {
        CameraCapturerCompat(context, CameraCapturer.CameraSource.FRONT_CAMERA)
    }
    private val videoConstraints: VideoConstraints by lazy {
        obtainVideoConstraints()
    }
    private val localVideoTrackNames: MutableMap<String, String> = HashMap()
//    private var screenVideoTrack: LocalVideoTrack? = null
//    private var restoreLocalVideoCameraTrack = false
//    private var screenVideoTrack: LocalVideoTrack? = null
//    private var screenCapturer: ScreenCapturer? = null
//    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
//        override fun onScreenCaptureError(errorDescription: String) {
//            Timber.e("Screen capturer error: %s", errorDescription)
//            stopScreenCapture()
//            Snackbar.make(
//                    primaryVideoView,
//                    R.string.screen_capture_error,
//                    Snackbar.LENGTH_LONG)
//                    .show()
//        }
//
//        override fun onFirstFrameAvailable() {
//            Timber.d("First frame from screen capturer available")
//        }
//    }
    private var isAudioMuted = false
    private var isVideoMuted = false

    fun onResume() {
        // TODO Check media permission before loading
        setupLocalAudioTrack()
        setupLocalVideoTrack()
        publishMediaTracks()
    }

    fun onPause() {
        removeCameraTrack()
    }

    private fun setupLocalAudioTrack() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)
            localAudioTrack?.let {
                Timber.e(RuntimeException(), "Failed to create local audio track")
            }
        }
    }

    private fun setupLocalVideoTrack() {
        if (!isVideoMuted) {
            cameraVideoTrack = LocalVideoTrack.create(
                    context,
                    true,
                    cameraCapturer.videoCapturer,
                    videoConstraints,
                    CAMERA_TRACK_NAME)
            cameraVideoTrack?.let {
                localVideoTrackNames[it.name] = context.getString(R.string.camera_video_track)
            } ?: run {
                Timber.e(RuntimeException(), "Failed to create local camera video track")
            }
        }
    }

    private fun publishMediaTracks() {
        localParticipant?.let { localParticipant ->
            localAudioTrack?.let { localParticipant.publishTrack(it) }
            cameraVideoTrack?.let { localParticipant.publishTrack(it)}
        }
    }

    private fun obtainVideoConstraints(): VideoConstraints {
        Timber.d("Collecting video constraints...")
        val builder = VideoConstraints.Builder()

        // setup aspect ratio
        sharedPreferences.getString(ASPECT_RATIO, "0")?.let { aspectRatio ->
            val aspectRatioIndex = aspectRatio.toInt()
            builder.aspectRatio(ASPECT_RATIOS[aspectRatioIndex])
            Timber.d("Aspect ratio : %s",
                    context.resources.getStringArray(
                            R.array.settings_screen_aspect_ratio_array)[aspectRatioIndex])
        }

        // setup video dimensions
        val minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, Preferences.MIN_VIDEO_DIMENSIONS_DEFAULT)
        val maxVideoDim = sharedPreferences.getInt(Preferences.MAX_VIDEO_DIMENSIONS, Preferences.MAX_VIDEO_DIMENSIONS_DEFAULT)
        builder.minVideoDimensions(Preferences.VIDEO_DIMENSIONS[minVideoDim])
        builder.maxVideoDimensions(Preferences.VIDEO_DIMENSIONS[maxVideoDim])

        Timber.d(
                "Video dimensions: %s - %s",
                context.resources
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[minVideoDim],
                context.resources
                        .getStringArray(R.array.settings_screen_video_dimensions_array)[maxVideoDim])

        // setup fps
        val minFps = sharedPreferences.getInt(Preferences.MIN_FPS, 0)
        val maxFps = sharedPreferences.getInt(Preferences.MAX_FPS, 24)
        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps)
            builder.maxFps(maxFps)
        }
        Timber.d("Frames per second: %d - %d", minFps, maxFps)
        return builder.build()
    }

    private fun removeCameraTrack() {
        cameraVideoTrack?.let { cameraVideoTrack ->
            localParticipant?.let { localParticipant ->
                localParticipant.unpublishTrack(cameraVideoTrack)
            }
            cameraVideoTrack.release()
            this.cameraVideoTrack = null
        }
    }

    // TODO Publish local tracks

    // TODO Save audio muted state upon config changes

    // TODO Teardown all tracks - i.e. call release and set to null

    // TODO Remove camera track when activity is in background

    // TODO Switch camera

    // TODO Handle screen share
    /*
     * screenCapturer = ScreenCapturer(this, resultCode, data, screenCapturerListener)
     * startScreenCapture()
     */

    // TODO Toggle local audio
    /*
     *         if (localAudioTrack == null) {
            isAudioMuted = false
            LocalAudioTrack.create(this, true, MICROPHONE_TRACK_NAME)?.let { localAudioTrack ->
                this.localAudioTrack = localAudioTrack
                localParticipant?.publishTrack(localAudioTrack)
            }
            icon = R.drawable.ic_mic_white_24px
            pauseAudioMenuItem.isVisible = true
            pauseAudioMenuItem.setTitle(
                    if (localAudioTrack?.isEnabled == true) R.string.pause_audio else R.string.resume_audio)
        } else {
            isAudioMuted = true

            localAudioTrack?.let { localAudioTrack ->
                localParticipant?.unpublishTrack(localAudioTrack)
                localAudioTrack.release()
                this.localAudioTrack = null
            }
            icon = R.drawable.ic_mic_off_gray_24px
            pauseAudioMenuItem.isVisible = false
        }
     */

    // TODO Toggle local video
    /*
     *        var newLocalVideoTrack: LocalVideoTrack? = null
        if (cameraVideoTrack == null) {
            isVideoMuted = false

            // add local camera track
            cameraCapturer?.let {
                cameraVideoTrack = LocalVideoTrack.create(
                        this,
                        true,
                        it.videoCapturer,
                        videoConstraints,
                        CAMERA_TRACK_NAME)
                newLocalVideoTrack = cameraVideoTrack
            }
            if (localParticipant != null) {
                cameraVideoTrack?.let { publishVideoTrack(it, TrackPriority.LOW) }

                // enable video settings
                val isCameraVideoTrackEnabled = cameraVideoTrack?.isEnabled == true
                switchCameraMenuItem.isVisible = isCameraVideoTrackEnabled
                pauseVideoMenuItem.setTitle(
                        if (isCameraVideoTrackEnabled) R.string.pause_video else R.string.resume_video)
                pauseVideoMenuItem.isVisible = true
            }
        } else {
            isVideoMuted = true
            // remove local camera track
            cameraVideoTrack?.let { cameraVideoTrack ->
                cameraVideoTrack.removeRenderer(primaryVideoView)
                localParticipant?.unpublishTrack(cameraVideoTrack)
                cameraVideoTrack.release()
                this.cameraVideoTrack = null
            }

            // disable video settings
            switchCameraMenuItem.isVisible = false
            pauseVideoMenuItem.isVisible = false
        }

        // update toggle button icon
        localVideoImageButton.setImageResource(
                if (cameraVideoTrack != null) R.drawable.ic_videocam_white_24px else R.drawable.ic_videocam_off_gray_24px)

        // Refresh view state
        localParticipant?.let { roomViewModel.processInput(ToggleLocalVideo(it.sid,
                (newLocalVideoTrack as VideoTrack?)?.let { VideoTrackViewState(it) }))
        }
                ?: roomViewModel.processInput(RefreshViewState)
     */

    // TODO Publish local tracks after connected to room

//
//
//    private fun startScreenCapture() {
//        screenCapturer?.let { screenCapturer ->
//            screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer,
//                    SCREEN_TRACK_NAME)
//            screenVideoTrack?.let { screenVideoTrack ->
//                screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp)
//                screenCaptureMenuItem.setTitle(R.string.stop_screen_share)
//                localVideoTrackNames[screenVideoTrack.name] = getString(R.string.screen_video_track)
//                if (localParticipant != null) {
//                    publishVideoTrack(screenVideoTrack, TrackPriority.HIGH)
//                }
//            } ?: run {
//                Snackbar.make(
//                        primaryVideoView,
//                        R.string.failed_to_add_screen_video_track,
//                        Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .show()
//            }
//        }
//    }
//
//    private fun stopScreenCapture() {
//        screenVideoTrack?.let { screenVideoTrack ->
//            localParticipant?.let { localParticipant ->
//                roomViewModel.processInput(RoomViewEvent.ScreenTrackRemoved(localParticipant.sid))
//                localParticipant.unpublishTrack(screenVideoTrack)
//            }
//            screenVideoTrack.release()
//            localVideoTrackNames.remove(screenVideoTrack.name)
//            this.screenVideoTrack = null
//            screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp)
//            screenCaptureMenuItem.setTitle(R.string.share_screen)
//        }
//    }
//
//
//    /** Try to restore camera video track after going to the settings screen or background  */
//    private fun restoreCameraTrack() {
//        if (restoreLocalVideoCameraTrack) {
//            obtainVideoConstraints()
//            setupLocalVideoTrack()
//            restoreLocalVideoCameraTrack = false
//        }
//    }
//
//    private fun setupLocalParticipant(room: Room) {
//        localParticipant = room.localParticipant
//        localParticipant?.let {
//            localParticipantSid = it.sid
//        }
//    }
//
//    private fun publishLocalTracks() {
//        if (localParticipant != null) {
//            cameraVideoTrack?.let { cameraVideoTrack ->
//                Timber.d("Camera track: %s", cameraVideoTrack)
//                publishVideoTrack(cameraVideoTrack, TrackPriority.LOW)
//            }
//            localAudioTrack?.let { localParticipant?.publishTrack(it) }
//        }
//    }
//
//    private fun publishVideoTrack(videoTrack: LocalVideoTrack, trackPriority: TrackPriority) {
//        val localTrackPublicationOptions = LocalTrackPublicationOptions(trackPriority)
//        localParticipant?.publishTrack(videoTrack, localTrackPublicationOptions)
//    }
//
//
//    /** Create local video track  */
//    private fun setupLocalVideoTrack() {
//
//        // initialize capturer only once if needed
//        if (cameraCapturer == null) {
//            cameraCapturer = CameraCapturerCompat(this, CameraCapturer.CameraSource.FRONT_CAMERA)
//        }
//        cameraCapturer?.let {
//            cameraVideoTrack = LocalVideoTrack.create(
//                    this,
//                    true,
//                    it.videoCapturer,
//                    videoConstraints,
//                    CAMERA_TRACK_NAME)
//        }
//        cameraVideoTrack?.let {
//            localVideoTrackNames[it.name] = getString(R.string.camera_video_track)
//        } ?: run {
//            Snackbar.make(
//                    primaryVideoView,
//                    R.string.failed_to_add_camera_video_track,
//                    Snackbar.LENGTH_LONG)
//                    .show()
//        }
//    }

}