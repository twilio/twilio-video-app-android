package com.twilio.video.app.videosdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twilio.androidenv.Env
import com.twilio.video.AspectRatio
import com.twilio.video.AudioCodec
import com.twilio.video.CameraCapturer
import com.twilio.video.ConnectOptions
import com.twilio.video.EncodingParameters
import com.twilio.video.G722Codec
import com.twilio.video.H264Codec
import com.twilio.video.IsacCodec
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.OpusCodec
import com.twilio.video.PcmaCodec
import com.twilio.video.PcmuCodec
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.ScreenCapturer
import com.twilio.video.TwilioException
import com.twilio.video.Video
import com.twilio.video.VideoCodec
import com.twilio.video.VideoConstraints
import com.twilio.video.VideoDimensions
import com.twilio.video.VideoTrack
import com.twilio.video.Vp8Codec
import com.twilio.video.Vp9Codec
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.model.RoomProperties
import com.twilio.video.app.data.api.model.Topology
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.EnvUtil
import com.twilio.video.app.util.plus
import com.twilio.video.app.videosdk.RoomViewEffect.RequestScreenSharePermission
import com.twilio.video.app.videosdk.RoomViewEffect.ScreenShareError
import com.twilio.video.app.videosdk.RoomViewEvent.SetupLocalMedia
import com.twilio.video.app.videosdk.RoomViewEvent.DisconnectFromRoom
import com.twilio.video.app.videosdk.RoomViewEvent.StartScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.ToggleSpeakerPhone
import com.twilio.video.app.videosdk.RoomViewEvent.ToggleLocalAudio
import com.twilio.video.app.videosdk.RoomViewEvent.StopScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.SetupScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.ConnectToRoom
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class RoomManager(
    private val sharedPreferences: SharedPreferences,
    private val tokenService: TokenService,
    private val context: Context
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
    private var cameraVideoTrack: LocalVideoTrack? = null
    private val restoreLocalVideoCameraTrack = false
    private var screenVideoTrack: LocalVideoTrack? = null
    private var cameraCapturer: CameraCapturerCompat? = null
    var screenCapturer: ScreenCapturer? = null
    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener {
        override fun onScreenCaptureError(errorDescription: String) {
            Timber.e("Screen capturer error: %s", errorDescription)
            stopScreenCapture()
            viewEffect(ScreenShareError)
        }

        override fun onFirstFrameAvailable() {
            Timber.d("First frame from screen capturer available")
        }
    }
    private val rxDisposables = CompositeDisposable()
    private val localVideoTrackNames: MutableMap<String, String> = HashMap()

    private val mutableViewState: MutableLiveData<RoomViewState> = MutableLiveData<RoomViewState>().apply { value = RoomViewState() }
    private val mutableViewEffects: MutableLiveData<RoomViewEffect> = MutableLiveData()

    val viewState: LiveData<RoomViewState> = mutableViewState
    val viewEffects: LiveData<RoomViewEffect> = mutableViewEffects

    fun processViewEvent(viewEvent: RoomViewEvent) {
        Timber.d("Processing ViewEvent: $viewEvent")
        when (viewEvent) {
            is SetupLocalMedia -> { setupLocalMedia(viewEvent.activity) }
            is ConnectToRoom -> { connectToRoom(viewEvent.roomName, viewEvent.tokenIdentity) }
            ToggleLocalAudio -> { toggleLocalAudio() }
            ToggleSpeakerPhone -> { toggleSpeakerPhone() }
            is SetupScreenCapture -> { setupScreenCapture(viewEvent.data) }
            StartScreenCapture -> { startScreenCapture() }
            StopScreenCapture -> { stopScreenCapture() }
            DisconnectFromRoom -> { disconnectFromRoom() }
        }
    }

    // TODO Remove activity dependency
    private fun setupLocalMedia(activity: Activity) {
        // Setup Audio
        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        savedVolumeControlStream = activity.volumeControlStream
        obtainVideoConstraints()
        localAudioTrack = LocalAudioTrack.create(activity, true, MICROPHONE_TRACK_NAME)

        // Setup Video
        setupLocalVideoTrack(context.getString(R.string.video_track))
        renderLocalParticipantStub(context.getString(R.string.you))
    }

    private fun connectToRoom(roomName: String, tokenIdentity: String) {
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

        rxDisposables + updateEnv()
                .andThen(tokenService.getToken(tokenIdentity, roomProperties))
                .onErrorResumeNext { e ->
                    Timber.e(e, "Fetch access token failed")
                    Single.error(e)
                }
                .flatMap { token -> connect(token, roomName) }
                .onErrorResumeNext { e ->
                    Timber.e(e, "Connection to room failed")
                    Single.error(e)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    updateState { it.copy(isConnectFailure = true) }
                }
                .subscribe()
    }

    private fun toggleLocalAudio() {
        if (localAudioTrack == null) {
            localParticipant?.let { localParticipant ->
                localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)
                localAudioTrack?.let { localParticipant.publishTrack(it) }
                updateState { it.copy(isLocalAudioMuted = false) }
            }
        } else {
            localAudioTrack?.let { localParticipant?.unpublishTrack(it) }
            localAudioTrack?.release()
            localAudioTrack = null
            updateState { it.copy(isLocalAudioMuted = true) }
        }
    }

    private fun toggleSpeakerPhone() {
        if (audioManager.isSpeakerphoneOn) {
            audioManager.isSpeakerphoneOn = false
            updateState { it.copy(isSpeakerPhoneMuted = true) }
        } else {
            audioManager.isSpeakerphoneOn = true
            updateState { it.copy(isSpeakerPhoneMuted = false) }
        }
    }

    private fun setupScreenCapture(data: Intent, resultCode: Int = Activity.RESULT_OK) {
        screenCapturer = ScreenCapturer(context, resultCode, data, screenCapturerListener)
    }

    private fun startScreenCapture() {
        screenCapturer?.let {
            screenVideoTrack = LocalVideoTrack.create(context, true, screenCapturer!!, SCREEN_TRACK_NAME)

            if (screenVideoTrack != null && screenCapturer != null) {
                localVideoTrackNames[screenVideoTrack!!.name] = context.getString(R.string.screen_video_track)
                localParticipant?.publishTrack(screenVideoTrack!!)
            } else {
                Timber.e("Failed to add screen video track")
            }
        } ?: run {
            viewEffect(RequestScreenSharePermission)
        }
    }

    private fun stopScreenCapture() {
        screenVideoTrack?.let { screenVideoTrack ->
            localParticipant?.unpublishTrack(screenVideoTrack)
            screenVideoTrack.release()
            localVideoTrackNames.remove(screenVideoTrack.name)
            this.screenVideoTrack = null
            updateState { it.copy(isScreenShared = false) }
        }
    }

    private fun disconnectFromRoom() {
        room?.disconnect()
        stopScreenCapture()

        // Reset the speakerphone
        audioManager.isSpeakerphoneOn = false

        // Teardown tracks
        localAudioTrack?.let {
            it.release()
            localAudioTrack = null
        }
        cameraVideoTrack?.let {
            it.release()
            cameraVideoTrack = null
        }
        screenVideoTrack?.let {
            it.release()
            screenVideoTrack = null
        }

        // dispose any token requests if needed
        rxDisposables.clear()
    }

    /**
     * Render local video track.
     *
     *
     * NOTE: Stub participant is created in controller. Make sure to remove it when connected to
     * room.
     */
    private fun renderLocalParticipantStub(localParticipantName: String) {
        cameraVideoTrack?.let { cameraVideoTrack ->
            val participantStub = ParticipantViewState(
                    localParticipantSid,
                    localParticipantName,
                    cameraVideoTrack,
                    null,
                    localAudioTrack == null,
                    cameraCapturer!!.cameraSource === CameraCapturer.CameraSource.FRONT_CAMERA
            )
            updateState { it.copy(primaryParticipant = participantStub) }
        } ?: run {
            Timber.e("Unable to create PrimaryParticipantViewState")
        }
    }

    private fun setupLocalVideoTrack(videoTrackName: String) {
        if (cameraCapturer == null) {
            cameraCapturer = CameraCapturerCompat(context, CameraCapturer.CameraSource.FRONT_CAMERA)
        }
        cameraCapturer?.let { cameraCapturer ->
            cameraVideoTrack = LocalVideoTrack.create(
                    context,
                    true,
                    cameraCapturer.videoCapturer,
                    videoConstraints,
                    CAMERA_TRACK_NAME)
        }
        cameraVideoTrack?.let { cameraVideoTrack ->
            localVideoTrackNames[cameraVideoTrack.name] = videoTrackName
            // Share camera video track if we are connected to room
            localParticipant?.publishTrack(cameraVideoTrack)
        } ?: run {
            Timber.e("Failed to add camera video track")
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
                Preferences.MAX_VIDEO_DIMENSIONS, videoDimensions.size - 1)
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

    private fun roomListener(): Room.Listener {
        return object : Room.Listener {
            override fun onConnected(room: Room) {
                updateState { it.copy(isConnected = true, isDisconnected = false, room = room) }
            }

            override fun onConnectFailure(
                room: Room,
                twilioException: TwilioException
            ) {
//                Timber.e(
//                        "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
//                        room.sid,
//                        room.state,
//                        twilioException.code,
//                        twilioException.message)
//                removeAllParticipants()
//                this@RoomActivity.room = null
//                updateUi(room)
//                setAudioFocus(false)
            }

            override fun onReconnecting(
                room: Room,
                twilioException: TwilioException
            ) {
                Timber.i("onReconnecting: %s", room.name)
            }

            override fun onReconnected(room: Room) {
                Timber.i("onReconnected: %s", room.name)
            }

            override fun onDisconnected(
                room: Room,
                twilioException: TwilioException?
            ) {
                Timber.i(
                        "Disconnected from room -> sid: %s, state: %s",
                        room.sid, room.state)
                updateState { it.copy(isConnected = false, isDisconnected = true, room = null) }
//                removeAllParticipants()
//                this@RoomActivity.room = null
//                this@RoomActivity.localParticipant = null
//                this@RoomActivity.localParticipantSid = LOCAL_PARTICIPANT_STUB_SID
//                updateUi(room)
//                updateStats()
//                setAudioFocus(false)
            }

            override fun onParticipantConnected(
                room: Room,
                remoteParticipant: RemoteParticipant
            ) {
                Timber.i(
                        "RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                        room.sid, remoteParticipant.sid)
                val renderAsPrimary = room.remoteParticipants.size == 1
//                addParticipant(remoteParticipant, renderAsPrimary)
//                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false))
            }

            override fun onParticipantDisconnected(
                room: Room,
                remoteParticipant: RemoteParticipant
            ) {
                Timber.i(
                        "RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                        room.sid, remoteParticipant.sid)
//                removeParticipant(remoteParticipant)
//                updateStatsUI(sharedPreferences.getBoolean(Preferences.ENABLE_STATS, false))
            }

            override fun onDominantSpeakerChanged(
                room: Room,
                remoteParticipant: RemoteParticipant?
            ) {
                if (remoteParticipant == null) {
//                    participantController.setDominantSpeaker(null)
                    return
                }
                val videoTrack: VideoTrack? = if (remoteParticipant.remoteVideoTracks.size > 0) remoteParticipant
                        .remoteVideoTracks[0]
                        .remoteVideoTrack else null
                if (videoTrack != null) {
//                    val participantView: ParticipantView = participantController.getThumb(remoteParticipant.sid, videoTrack)
//                    if (participantView != null) {
//                        participantController.setDominantSpeaker(participantView)
//                    } else {
//                        remoteParticipant.identity
//                        val primaryParticipantView: ParticipantPrimaryView = participantController.getPrimaryView()
//                        if (primaryParticipantView.identity ==
//                                remoteParticipant.identity) {
//                            participantController.setDominantSpeaker(
//                                    participantController.getPrimaryView())
//                        } else {
//                            participantController.setDominantSpeaker(null)
//                        }
//                    }
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

    private fun connect(token: String, roomName: String): Single<Room>? {
        return Single.fromCallable {
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
            localAudioTrack?.let {
                connectOptionsBuilder.audioTracks(listOf(it))
            }
            val localVideoTracks: MutableList<LocalVideoTrack> = ArrayList()
            cameraVideoTrack?.let {
                localVideoTracks.add(it)
            }
            screenVideoTrack?.let {
                localVideoTracks.add(it)
            }
            if (localVideoTracks.isNotEmpty()) {
                connectOptionsBuilder.videoTracks(localVideoTracks)
            }
            connectOptionsBuilder.preferVideoCodecs(listOf(preferedVideoCodec))
            connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))
            connectOptionsBuilder.encodingParameters(encodingParameters)
            room = Video.connect(
                    context,
                    connectOptionsBuilder.build(),
                    roomListener())
            room
        }
    }

    private fun updateEnv(): Completable {
        return Completable.fromAction {
            val env = sharedPreferences.getString(
                    Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT)
            val nativeEnvironmentVariableValue = EnvUtil.getNativeEnvironmentVariableValue(env)
            Env.set(
                    context,
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

    private fun updateState(action: (oldState: RoomViewState) -> RoomViewState) {
        val oldState = mutableViewState.value
        oldState?.let {
            mutableViewState.value = action(oldState)
        }
    }

    private fun viewEffect(roomViewEffect: RoomViewEffect) {
        mutableViewEffects.value = roomViewEffect
    }
}