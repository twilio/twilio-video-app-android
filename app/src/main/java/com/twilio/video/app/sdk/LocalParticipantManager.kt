package com.twilio.video.app.sdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalTrackPublicationOptions
import com.twilio.video.LocalVideoTrack
import com.twilio.video.ScreenCapturer
import com.twilio.video.TrackPriority
import com.twilio.video.VideoFormat
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION_DEFAULT
import com.twilio.video.app.data.Preferences.VIDEO_DIMENSIONS
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioDisabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioEnabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioOff
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioOn
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.ScreenCaptureOff
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.ScreenCaptureOn
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoDisabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoEnabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoTrackUpdated
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.get
import timber.log.Timber

class LocalParticipantManager(
    private val context: Context,
    private val roomManager: RoomManager,
    private val sharedPreferences: SharedPreferences
) {

    private var localAudioTrack: LocalAudioTrack? = null
        set(value) {
            field = value
            roomManager.sendRoomEvent(if (value == null) AudioOff else AudioOn)
        }
    internal var localParticipant: LocalParticipant? = null
    private var cameraVideoTrack: LocalVideoTrack? = null
        set(value) {
            field = value
            roomManager.sendRoomEvent(VideoTrackUpdated(value))
        }
    private var cameraCapturer: CameraCapturerCompat? = null
    private var screenCapturer: ScreenCapturer? = null
    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
        override fun onScreenCaptureError(errorDescription: String) {
            Timber.e(RuntimeException(), "Screen capturer error: %s", errorDescription)
            stopScreenCapture()
        }

        override fun onFirstFrameAvailable() {}
    }
    private var screenVideoTrack: LocalVideoTrack? = null
        set(value) {
            field = value
            roomManager.sendRoomEvent(if (value == null) ScreenCaptureOff else ScreenCaptureOn)
        }
    private var isAudioMuted = false
    private var isVideoMuted = false
    internal val localVideoTrackNames: MutableMap<String, String> = HashMap()

    fun onResume() {
        if (!isAudioMuted) setupLocalAudioTrack()
        if (!isVideoMuted) setupLocalVideoTrack()
    }

    fun onPause() {
        removeCameraTrack()
    }

    fun toggleLocalVideo() {
        if (!isVideoMuted) {
            isVideoMuted = true
            removeCameraTrack()
        } else {
            isVideoMuted = false
            setupLocalVideoTrack()
        }
    }

    fun enableLocalVideo() {
        cameraVideoTrack?.enable(true)
        roomManager.sendRoomEvent(VideoEnabled)
    }

    fun disableLocalVideo() {
        cameraVideoTrack?.enable(false)
        roomManager.sendRoomEvent(VideoDisabled)
    }

    fun enableLocalAudio() {
        localAudioTrack?.enable(true)
        roomManager.sendRoomEvent(AudioEnabled)
    }

    fun disableLocalAudio() {
        localAudioTrack?.enable(false)
        roomManager.sendRoomEvent(AudioDisabled)
    }

    fun toggleLocalAudio() {
        if (!isAudioMuted) {
            isAudioMuted = true
            removeAudioTrack()
        } else {
            isAudioMuted = false
            setupLocalAudioTrack()
        }
    }

    fun startScreenCapture(captureResultCode: Int, captureIntent: Intent) {
        screenCapturer = ScreenCapturer(context, captureResultCode, captureIntent,
                screenCapturerListener)
        screenCapturer?.let { screenCapturer ->
            screenVideoTrack = LocalVideoTrack.create(context, true, screenCapturer,
                    SCREEN_TRACK_NAME)
            screenVideoTrack?.let { screenVideoTrack ->
                localVideoTrackNames[screenVideoTrack.name] =
                        context.getString(R.string.screen_video_track)
                localParticipant?.publishTrack(screenVideoTrack,
                        LocalTrackPublicationOptions(TrackPriority.HIGH))
            } ?: Timber.e(RuntimeException(), "Failed to add screen video track")
        }
    }

    fun stopScreenCapture() {
        screenVideoTrack?.let { screenVideoTrack ->
            localParticipant?.unpublishTrack(screenVideoTrack)
            screenVideoTrack.release()
            localVideoTrackNames.remove(screenVideoTrack.name)
            this.screenVideoTrack = null
        }
    }

    fun publishLocalTracks() {
        publishAudioTrack(localAudioTrack)
        publishCameraTrack(cameraVideoTrack)
    }

    fun switchCamera() = cameraCapturer?.switchCamera()

    private fun setupLocalAudioTrack() {
        if (localAudioTrack == null && !isAudioMuted) {
            localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)
            localAudioTrack?.let { publishAudioTrack(it) }
                    ?: Timber.e(RuntimeException(), "Failed to create local audio track")
        }
    }

    private fun publishCameraTrack(localVideoTrack: LocalVideoTrack?) {
        if (!isVideoMuted) {
            localVideoTrack?.let {
                localParticipant?.publishTrack(it,
                        LocalTrackPublicationOptions(TrackPriority.LOW))
            }
        }
    }

    private fun publishAudioTrack(localAudioTrack: LocalAudioTrack?) {
        if (!isAudioMuted) {
            localAudioTrack?.let { localParticipant?.publishTrack(it) }
        }
    }

    private fun unpublishTrack(localVideoTrack: LocalVideoTrack?) =
            localVideoTrack?.let { localParticipant?.unpublishTrack(it) }

    private fun unpublishTrack(localAudioTrack: LocalAudioTrack?) =
            localAudioTrack?.let { localParticipant?.unpublishTrack(it) }

    private fun setupLocalVideoTrack() {
        val dimensionsIndex = sharedPreferences.get(VIDEO_CAPTURE_RESOLUTION,
                VIDEO_CAPTURE_RESOLUTION_DEFAULT).toInt()
        val videoFormat = VideoFormat(VIDEO_DIMENSIONS[dimensionsIndex], 30)

        cameraCapturer = CameraCapturerCompat.newInstance(context)
        cameraVideoTrack = cameraCapturer?.let { cameraCapturer ->
            LocalVideoTrack.create(
                    context,
                    true,
                    cameraCapturer,
                    videoFormat,
                    CAMERA_TRACK_NAME)
        }
        cameraVideoTrack?.let { cameraVideoTrack ->
            localVideoTrackNames[cameraVideoTrack.name] = context.getString(R.string.camera_video_track)
            publishCameraTrack(cameraVideoTrack)
        } ?: run {
            Timber.e(RuntimeException(), "Failed to create the local camera video track")
        }
    }

    private fun removeCameraTrack() {
        cameraVideoTrack?.let { cameraVideoTrack ->
            unpublishTrack(cameraVideoTrack)
            localVideoTrackNames.remove(cameraVideoTrack.name)
            cameraVideoTrack.release()
            this.cameraVideoTrack = null
        }
    }

    private fun removeAudioTrack() {
        localAudioTrack?.let { localAudioTrack ->
            unpublishTrack(localAudioTrack)
            localAudioTrack.release()
            this.localAudioTrack = null
        }
    }
}
