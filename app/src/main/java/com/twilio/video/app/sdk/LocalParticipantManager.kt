package com.twilio.video.app.sdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalTrackPublicationOptions
import com.twilio.video.LocalVideoTrack
import com.twilio.video.ScreenCapturer
import com.twilio.video.TrackPriority
import com.twilio.video.VideoFormat
import com.twilio.video.VideoFrameProcessor
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.CAPTURER_EFFECTS
import com.twilio.video.app.data.Preferences.CAPTURER_EFFECTS_DEFAULT
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
import com.twilio.video.ktx.AudioOptionsBuilder
import com.twilio.video.ktx.createLocalAudioTrack
import com.twilio.video.ktx.createLocalVideoTrack
import com.twilio.video.virtualbackgroundprocessor.BlurBackgroundVideoFrameProcessor
import com.twilio.video.virtualbackgroundprocessor.VirtualBackgroundVideoFrameProcessor
import timber.log.Timber

class LocalParticipantManager(
    private val context: Context,
    private val roomManager: RoomManager,
    private val sharedPreferences: SharedPreferences,
) {

    // DEBUG
    private var videoFrameProcessor: VideoFrameProcessor? = null

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
            /* re-enable to test on-the-fly changing background processor config
            if (videoFrameProcessor is BlurBackgroundVideoFrameProcessor) {
                (videoFrameProcessor as BlurBackgroundVideoFrameProcessor).blurFilterRadius = 32;
                (videoFrameProcessor as BlurBackgroundVideoFrameProcessor).blurFilterSigma = 27.5f;
            }
            if (videoFrameProcessor is VirtualBackgroundVideoFrameProcessor) {
                (videoFrameProcessor as VirtualBackgroundVideoFrameProcessor).background =
                    BitmapFactory.decodeResource(context.resources, R.drawable.halfdome_720p);
            }
             */
            removeAudioTrack()
        } else {
            isAudioMuted = false
            /* re-enable to test on-the-fly changing background processor config
            if (videoFrameProcessor is BlurBackgroundVideoFrameProcessor) {
                (videoFrameProcessor as BlurBackgroundVideoFrameProcessor).blurFilterRadius = 15;
            }
            if (videoFrameProcessor is VirtualBackgroundVideoFrameProcessor) {
                (videoFrameProcessor as VirtualBackgroundVideoFrameProcessor).background =
                    BitmapFactory.decodeResource(context.resources, R.drawable.mt_whitney_720p);
            }
             */
            setupLocalAudioTrack()
        }
    }

    fun startScreenCapture(captureResultCode: Int, captureIntent: Intent) {
        screenCapturer = ScreenCapturer(
            context,
            captureResultCode,
            captureIntent,
            screenCapturerListener,
        )
        screenCapturer?.let { screenCapturer ->
            screenVideoTrack = createLocalVideoTrack(
                context,
                true,
                screenCapturer,
                name = SCREEN_TRACK_NAME,
            )
            screenVideoTrack?.let { screenVideoTrack ->
                localVideoTrackNames[screenVideoTrack.name] =
                    context.getString(R.string.screen_video_track)
                localParticipant?.publishTrack(
                    screenVideoTrack,
                    LocalTrackPublicationOptions(TrackPriority.HIGH),
                )
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
            var audioOptions: AudioOptionsBuilder = {
                echoCancellation(
                    sharedPreferences.getBoolean(
                        Preferences.AUDIO_ACOUSTIC_ECHO_CANCELER,
                        Preferences.AUDIO_ACOUSTIC_ECHO_CANCELER_DEFAULT,
                    ),
                )
                noiseSuppression(
                    sharedPreferences.getBoolean(
                        Preferences.AUDIO_ACOUSTIC_NOISE_SUPRESSOR,
                        Preferences.AUDIO_ACOUSTIC_NOISE_SUPRESSOR_DEFAULT,
                    ),
                )
                autoGainControl(
                    sharedPreferences.getBoolean(
                        Preferences.AUDIO_AUTOMATIC_GAIN_CONTROL,
                        Preferences.AUDIO_AUTOMATIC_GAIN_CONTROL_DEFAULT,
                    ),
                )
            }
            localAudioTrack = createLocalAudioTrack(context, true, MICROPHONE_TRACK_NAME, audioOptions)
            localAudioTrack?.let { publishAudioTrack(it) }
                ?: Timber.e(RuntimeException(), "Failed to create local audio track")
        }
    }

    private fun publishCameraTrack(localVideoTrack: LocalVideoTrack?) {
        if (!isVideoMuted) {
            localVideoTrack?.let {
                localParticipant?.publishTrack(
                    it,
                    LocalTrackPublicationOptions(TrackPriority.LOW),
                )
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
        val dimensionsIndex = sharedPreferences.get(
            VIDEO_CAPTURE_RESOLUTION,
            VIDEO_CAPTURE_RESOLUTION_DEFAULT,
        ).toInt()
        val videoFormat = VideoFormat(VIDEO_DIMENSIONS[dimensionsIndex], 30)

        val selectedEffects = sharedPreferences.get(
            CAPTURER_EFFECTS,
            CAPTURER_EFFECTS_DEFAULT,
        )
        val videoEffectsProcessor = createVideoFrameProcessor(selectedEffects)

        // DEBUG: Remove when done testing Virtual background 'on-the-fly' config
        this.videoFrameProcessor = videoEffectsProcessor

        cameraCapturer = CameraCapturerCompat.newInstance(context, videoEffectsProcessor)
        cameraVideoTrack = cameraCapturer?.let { cameraCapturer ->
            LocalVideoTrack.create(
                context,
                true,
                cameraCapturer,
                videoFormat,
                CAMERA_TRACK_NAME,
            )
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

    private fun createVideoFrameProcessor(type: String): VideoFrameProcessor? {
        return when (type) {
            BlurBackgroundVideoFrameProcessor::class.simpleName ->
                BlurBackgroundVideoFrameProcessor(context, 15, 7.5f)

            VirtualBackgroundVideoFrameProcessor::class.simpleName -> {
                VirtualBackgroundVideoFrameProcessor(
                    context,
                    BitmapFactory.decodeResource(context.resources, R.drawable.mt_whitney_720p),
                )
            }

            else -> null
        }
    }
}
