package com.twilio.video.app.videosdk

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twilio.androidenv.Env
import com.twilio.video.*
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.model.RoomProperties
import com.twilio.video.app.data.api.model.Topology
import com.twilio.video.app.ui.room.ParticipantPrimaryView
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.EnvUtil
import com.twilio.video.app.util.plus
import com.twilio.video.app.videosdk.RoomEvent.Connected
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

class RoomManager(
        private val sharedPreferences: SharedPreferences,
        private val tokenService: TokenService
) {
    private val STATS_DELAY = 1000 // milliseconds

    private val MICROPHONE_TRACK_NAME = "microphone"
    private val CAMERA_TRACK_NAME = "camera"
    private val SCREEN_TRACK_NAME = "screen"

    // This will be used instead of real local participant sid,
// because that information is unknown until room connection is fully established
    private val LOCAL_PARTICIPANT_STUB_SID = ""

    private val aspectRatios = arrayOf(AspectRatio.ASPECT_RATIO_4_3, AspectRatio.ASPECT_RATIO_16_9, AspectRatio.ASPECT_RATIO_11_9)

    private val videoDimensions = arrayOf(
            VideoDimensions.CIF_VIDEO_DIMENSIONS,
            VideoDimensions.VGA_VIDEO_DIMENSIONS,
            VideoDimensions.WVGA_VIDEO_DIMENSIONS,
            VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
    )

    private lateinit var audioManager: AudioManager
    private val savedAudioMode = AudioManager.MODE_INVALID
    private var savedVolumeControlStream = 0
    private val savedIsMicrophoneMute = false
    private val savedIsSpeakerPhoneOn = false

    private val localParticipant: LocalParticipant? = null
    private val localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
    private var room: Room? = null
    private var videoConstraints: VideoConstraints? = null
    private var localAudioTrack: LocalAudioTrack? = null
    private val cameraVideoTrack: LocalVideoTrack? = null
    private val restoreLocalVideoCameraTrack = false
    private var screenVideoTrack: LocalVideoTrack? = null
    private val cameraCapturer: CameraCapturerCompat? = null
    private val screenCapturer: ScreenCapturer? = null
    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
        override fun onScreenCaptureError(errorDescription: String) {
            Timber.e("Screen capturer error: %s", errorDescription)
            stopScreenCapture()
            mutableRoomEvents.value = RoomEvent.ScreenShareError
        }

        override fun onFirstFrameAvailable() {
            Timber.d("First frame from screen capturer available")
        }
    }

    private val rxDisposables = CompositeDisposable()
    private val localVideoTrackNames: Map<String, String> = HashMap()
    private val mutableRoomEvents: MutableLiveData<RoomEvent> = MutableLiveData()
    val roomEvents: LiveData<RoomEvent> = mutableRoomEvents

    fun init(displayName: String?, context: Activity) {
        this.displayName = displayName
        // Setup Audio
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        savedVolumeControlStream = context.volumeControlStream
        obtainVideoConstraints()
    }


    fun connectToRoom(activity: Activity, roomName: String, tokenIdentity: String) {
        // obtain latest environment preferences
        val roomProperties =
                RoomProperties.Builder()
                        .setName(roomName)
                        .setTopology(
                                Topology.fromString(
                                        sharedPreferences.getString(
                                                Preferences.TOPOLOGY,
                                                Preferences.TOPOLOGY_DEFAULT)))
                        .setRecordOnParticipantsConnect(
                                sharedPreferences.getBoolean(
                                        Preferences.RECORD_PARTICIPANTS_ON_CONNECT,
                                        Preferences.RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT))
                        .createRoomProperties()

        rxDisposables + updateEnv(activity)
                .andThen(tokenService.getToken(tokenIdentity, roomProperties))
                .onErrorResumeNext { e ->
                    Timber.e(e, "Fetch access token failed")
                    Single.error(e)
                }
                .flatMap { token -> connect(activity, token, roomName }
                .onErrorResumeNext { e ->
                    Timber.e(e, "Connection to room failed")
                    Single.error(e)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    mutableRoomEvents.value = RoomEvent.ConnectFailure
                }
                .subscribe()
    }

    fun toggleLocalAudio(context: Context) {
        localAudioTrack?.let { localAudioTrack ->
            localParticipant?.let { localParticipant ->
                this.localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)
                localParticipant.publishTrack(localAudioTrack)
            }
        } ?: run {
            localParticipant?.unpublishTrack(localAudioTrack!!)
            localAudioTrack!!.release()
            localAudioTrack = null
        }

    }

    fun disconnect() {
        room?.disconnect()
        stopScreenCapture()
    }

    private fun stopScreenCapture() {
        if (screenVideoTrack != null) {
            localParticipant?.unpublishTrack(screenVideoTrack)
            screenVideoTrack.release()
            localVideoTrackNames.remove(screenVideoTrack.getName())
            screenVideoTrack = null
            screenCaptureMenuItem.setIcon(R.drawable.ic_screen_share_white_24dp)
            screenCaptureMenuItem.setTitle(R.string.share_screen)
        }
    }

    private fun obtainVideoConstraints() {
        Timber.d("Collecting video constraints...")
        val builder = VideoConstraints.Builder()
        // setup aspect ratio
        val aspectRatio = sharedPreferences.getString(Preferences.ASPECT_RATIO, "0")
        if (aspectRatio != null) {
            val aspectRatioIndex = aspectRatio.toInt()
            builder.aspectRatio(aspectRatios[aspectRatioIndex])
        }
        // setup video dimensions
        val minVideoDim = sharedPreferences.getInt(Preferences.MIN_VIDEO_DIMENSIONS, 0)
        val maxVideoDim = sharedPreferences.getInt(
                Preferences.MAX_VIDEO_DIMENSIONS, videoDimensions.length - 1)
        if (maxVideoDim != -1 && minVideoDim != -1) {
            builder.minVideoDimensions(videoDimensions[minVideoDim])
            builder.maxVideoDimensions(videoDimensions[maxVideoDim])
        }
        // setup fps
        val minFps = sharedPreferences.getInt(Preferences.MIN_FPS, 0)
        val maxFps = sharedPreferences.getInt(Preferences.MAX_FPS, 30)
        if (maxFps != -1 && minFps != -1) {
            builder.minFps(minFps)
            builder.maxFps(maxFps)
        }
        Timber.d("Frames per second: %d - %d", minFps, maxFps)
        videoConstraints = builder.build()
    }

    private fun roomListener(): Room.Listener? {
        return object : Room.Listener {
            override fun onConnected(room: Room) {
                mutableRoomEvents.value = Connected(room)
            }

            override fun onConnectFailure(
                    room: Room, twilioException: TwilioException) {
                Timber.e(
                        "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                        room.sid,
                        room.state,
                        twilioException.code,
                        twilioException.message)
                removeAllParticipants()
                this@RoomActivity.room = null
                updateUi(room)
                setAudioFocus(false)
            }

            override fun onReconnecting(
                    room: Room, twilioException: TwilioException) {
                Timber.i("onReconnecting: %s", room.name)
            }

            override fun onReconnected(room: Room) {
                Timber.i("onReconnected: %s", room.name)
            }

            override fun onDisconnected(
                    room: Room, twilioException: TwilioException?) {
                Timber.i(
                        "Disconnected from room -> sid: %s, state: %s",
                        room.sid, room.state)
                removeAllParticipants()
                this@RoomActivity.room = null
                this@RoomActivity.localParticipant = null
                this@RoomActivity.localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
                updateUi(room)
                updateStats()
                setAudioFocus(false)
            }

            override fun onParticipantConnected(
                    room: Room, remoteParticipant: RemoteParticipant) {
                Timber.i(
                        "RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                        room.sid, remoteParticipant.sid)
                val renderAsPrimary = room.remoteParticipants.size == 1
                addParticipant(remoteParticipant, renderAsPrimary)
                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false))
            }

            override fun onParticipantDisconnected(
                    room: Room, remoteParticipant: RemoteParticipant) {
                Timber.i(
                        "RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                        room.sid, remoteParticipant.sid)
                removeParticipant(remoteParticipant)
                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false))
            }

            override fun onDominantSpeakerChanged(
                    room: Room, remoteParticipant: RemoteParticipant?) {
                if (remoteParticipant == null) {
                    participantController.setDominantSpeaker(null)
                    return
                }
                val videoTrack: VideoTrack? = if (remoteParticipant.remoteVideoTracks.size > 0) remoteParticipant
                        .remoteVideoTracks[0]
                        .remoteVideoTrack else null
                if (videoTrack != null) {
                    val participantView: ParticipantView = participantController.getThumb(remoteParticipant.sid, videoTrack)
                    if (participantView != null) {
                        participantController.setDominantSpeaker(participantView)
                    } else {
                        remoteParticipant.identity
                        val primaryParticipantView: ParticipantPrimaryView = participantController.getPrimaryView()
                        if (primaryParticipantView.identity ==
                                remoteParticipant.identity) {
                            participantController.setDominantSpeaker(
                                    participantController.getPrimaryView())
                        } else {
                            participantController.setDominantSpeaker(null)
                        }
                    }
                }
            }

            override fun onRecordingStarted(room: Room) {
                Timber.i("onRecordingStarted: %s", room.name)
            }

            override fun onRecordingStopped(room: Room) {
                Timber.i("onRecordingStopped: %s", room.name)
            }
        }
    }

    private fun connect(activity: Activity, token: String, roomName: String): Single<Room>? {
        return Single.fromCallable {
            val env = sharedPreferences.getString(
                    Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT)
            val enableInsights = sharedPreferences.getBoolean(
                    Preferences.ENABLE_INSIGHTS,
                    Preferences.ENABLE_INSIGHTS_DEFAULT)
            val enableAutomaticTrackSubscription: Boolean = getPreferenceByKeyWithDefault(
                    Preferences.ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION,
                    Preferences.ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION_DEFAULT)
            val enableDominantSpeaker: Boolean = getPreferenceByKeyWithDefault(
                    Preferences.ENABLE_DOMINANT_SPEAKER,
                    Preferences.ENABLE_DOMINANT_SPEAKER_DEFAULT)
            val preferedVideoCodec: VideoCodec = getVideoCodecPreference(Preferences.VIDEO_CODEC)
            val preferredAudioCodec: AudioCodec = getAudioCodecPreference()
            val connectOptionsBuilder = ConnectOptions.Builder(token)
                    .roomName(roomName)
                    .enableAutomaticSubscription(enableAutomaticTrackSubscription)
                    .enableDominantSpeaker(enableDominantSpeaker)
                    .enableInsights(enableInsights)
                    .enableNetworkQuality(isNetworkQualityEnabled())
            val maxVideoBitrate = sharedPreferences.getInt(
                    Preferences.MAX_VIDEO_BITRATE,
                    Preferences.MAX_VIDEO_BITRATE_DEFAULT)
            val maxAudioBitrate = sharedPreferences.getInt(
                    Preferences.MAX_AUDIO_BITRATE,
                    Preferences.MAX_AUDIO_BITRATE_DEFAULT)
            val encodingParameters = EncodingParameters(maxAudioBitrate, maxVideoBitrate)
            if (localAudioTrack != null) {
                connectOptionsBuilder.audioTracks(listOf(localAudioTrack))
            }
            val localVideoTracks: MutableList<LocalVideoTrack> = ArrayList()
            if (cameraVideoTrack != null) {
                localVideoTracks.add(cameraVideoTrack)
            }
            if (screenVideoTrack != null) {
                localVideoTracks.add(screenVideoTrack!!)
            }
            if (!localVideoTracks.isEmpty()) {
                connectOptionsBuilder.videoTracks(localVideoTracks)
            }
            connectOptionsBuilder.preferVideoCodecs(listOf(preferedVideoCodec))
            connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))
            connectOptionsBuilder.encodingParameters(encodingParameters)
            room = Video.connect(
                    activity,
                    connectOptionsBuilder.build(),
                    roomListener()!!)
            room
        }
    }

    private fun updateEnv(activity: Activity): Completable {
        return Completable.fromAction {
            val env = sharedPreferences.getString(
                    Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT)
            val nativeEnvironmentVariableValue = EnvUtil.getNativeEnvironmentVariableValue(env)
            Env.set(
                    activity,
                    EnvUtil.TWILIO_ENV_KEY,
                    nativeEnvironmentVariableValue,
                    true)
        }
    }

    private fun getPreferenceByKeyWithDefault(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun getVideoCodecPreference(key: String): VideoCodec {
        val videoCodecName = sharedPreferences.getString(key, Preferences.VIDEO_CODEC_DEFAULT)
        return if (videoCodecName != null) {
            when (videoCodecName) {
                Vp8Codec.NAME -> {
                    val simulcast = sharedPreferences.getBoolean(
                            Preferences.VP8_SIMULCAST, Preferences.VP8_SIMULCAST_DEFAULT)
                    Vp8Codec(simulcast)
                }
                H264Codec.NAME -> H264Codec()
                Vp9Codec.NAME -> Vp9Codec()
                else -> Vp8Codec()
            }
        } else {
            Vp8Codec()
        }
    }

    private fun getAudioCodecPreference(): AudioCodec {
        val audioCodecName = sharedPreferences.getString(
                Preferences.AUDIO_CODEC, Preferences.AUDIO_CODEC_DEFAULT)
        return if (audioCodecName != null) {
            when (audioCodecName) {
                IsacCodec.NAME -> IsacCodec()
                PcmaCodec.NAME -> PcmaCodec()
                PcmuCodec.NAME -> PcmuCodec()
                G722Codec.NAME -> G722Codec()
                else -> OpusCodec()
            }
        } else {
            OpusCodec()
        }
    }

    private fun isNetworkQualityEnabled(): Boolean {
        return sharedPreferences.getBoolean(
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL,
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT)
    }
}