package com.twilio.video.app.videosdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
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
import com.twilio.video.app.videosdk.RoomViewEvent.ConnectToRoom
import com.twilio.video.app.videosdk.RoomViewEvent.DisconnectFromRoom
import com.twilio.video.app.videosdk.RoomViewEvent.SetupLocalMedia
import com.twilio.video.app.videosdk.RoomViewEvent.SetupScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.StartScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.StopScreenCapture
import com.twilio.video.app.videosdk.RoomViewEvent.TearDownLocalMedia
import com.twilio.video.app.videosdk.RoomViewEvent.ToggleLocalAudio
import com.twilio.video.app.videosdk.RoomViewEvent.ToggleSpeakerPhone
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
    private var savedAudioMode = AudioManager.MODE_INVALID
    private var savedVolumeControlStream = 0
    private var savedIsMicrophoneMute = false
    private var savedIsSpeakerPhoneOn = false

    private var localParticipant: LocalParticipant? = null
    private var localParticipantSid: String? = LOCAL_PARTICIPANT_STUB_SID
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
            is SetupLocalMedia -> {
                setupLocalMedia(viewEvent.volumeControlStream)
            }
            TearDownLocalMedia -> {
                tearDownLocalMedia()
            }
            is ConnectToRoom -> {
                connectToRoom(viewEvent.roomName, viewEvent.tokenIdentity)
            }
            DisconnectFromRoom -> {
                disconnectFromRoom()
            }
            ToggleLocalAudio -> {
                toggleLocalAudio()
            }
            ToggleSpeakerPhone -> {
                toggleSpeakerPhone()
            }
            is SetupScreenCapture -> {
                setupScreenCapture(viewEvent.data)
            }
            StartScreenCapture -> {
                startScreenCapture()
            }
            StopScreenCapture -> {
                stopScreenCapture()
            }
        }
    }

    private fun tearDownLocalMedia() {
        // Reset the speakerphone
        audioManager.isSpeakerphoneOn = false
        // Teardown tracks
        localAudioTrack?.let { localAudioTrack ->
            localAudioTrack.release()
            localParticipant?.unpublishTrack(localAudioTrack)
            this.localAudioTrack = null
        }
        cameraVideoTrack?.let { cameraVideoTrack ->
            cameraVideoTrack.release()
            localParticipant?.unpublishTrack(cameraVideoTrack)
            this.cameraVideoTrack = null
        }
        screenVideoTrack?.let { screenVideoTrack ->
            screenVideoTrack.release()
            localParticipant?.unpublishTrack(screenVideoTrack)
            this.screenVideoTrack = null
        }
        // dispose any token requests if needed
        rxDisposables.clear()
    }

    private fun setupLocalMedia(volumeControlStream: Int) {
        // Setup Audio
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true
        savedVolumeControlStream = volumeControlStream
        obtainVideoConstraints()
        localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)

        // Setup Video
        setupLocalVideoTrack(context.getString(R.string.video_track))
        renderLocalParticipantStub(context.getString(R.string.you))
    }

    private fun connectToRoom(roomName: String, tokenIdentity: String) {
        // obtain latest environment preferences
        if (roomName.isNotBlank()) {
            updateState { it.copy(isConnecting = true) }
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
    }

    private fun toggleLocalAudio() {
        if (localAudioTrack == null) {
            localAudioTrack = LocalAudioTrack.create(context, true, MICROPHONE_TRACK_NAME)
            localAudioTrack?.let { localAudioTrack ->
                localParticipant?.publishTrack(localAudioTrack)
                updateState { it.copy(isLocalAudioMuted = false) }
            }
        } else {
            localAudioTrack?.let { localAudioTrack ->
                localParticipant?.unpublishTrack(localAudioTrack)
                localAudioTrack.release()
            }
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

        updateState { it.copy(participants = emptyList()) }

        stopScreenCapture()

        setAudioFocus(false)

        // Reset the speakerphone
        audioManager.isSpeakerphoneOn = false

        // Teardown tracks
        localAudioTrack?.let {
            it.release()
            localAudioTrack = null
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
            localParticipantSid?.let { localParticipantSid ->
                val participantStub = ParticipantViewState(
                        localParticipantSid,
                        localParticipantName,
                        cameraVideoTrack,
                        null,
                        localAudioTrack == null,
                        cameraCapturer!!.cameraSource === CameraCapturer.CameraSource.FRONT_CAMERA
                )
                updateState { it.copy(primaryParticipant = participantStub) }
            } ?: Timber.e("LocalParticipantSid is null")
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
                initializeRoom()
                updateState {
                    it.copy(isConnected = true,
                            isConnecting = false,
                            isDisconnected = false,
                            isConnectFailure = false,
                            room = room)
                }
            }

            override fun onConnectFailure(
                room: Room,
                twilioException: TwilioException
            ) {
                updateState {
                    it.copy(isConnected = false,
                            isConnecting = false,
                            isDisconnected = false,
                            isConnectFailure = true,
                            room = room)
                }
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
                updateState {
                    it.copy(isConnected = false,
                            isConnecting = false,
                            isDisconnected = true,
                            isConnectFailure = false,
                            room = room)
                }
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

    private fun initializeRoom() {
        room?.let { room ->
            Timber.i(
                    "Connected to room -> name: %s, sid: %s, state: %s",
                    room.name, room.sid, room.state)
            this.localParticipant = room.localParticipant
            localParticipantSid = localParticipant?.sid

            setAudioFocus(true)
//            updateStats();

            // remove primary view
//            participantController.removePrimary();

            // add local thumb and "click" on it to make primary
            withState { viewState ->
                val primaryParticipant = viewState.primaryParticipant
                val participants = mutableListOf<ParticipantViewState>()
                primaryParticipant?.let { participants.add(it) }
                updateState { it.copy(participants = participants) }
            }

//            participantController.addThumb(
//                    localParticipantSid,
//                    getString(R.string.you),
//                    cameraVideoTrack,
//                    localAudioTrack == null,
//                    cameraCapturer.getCameraSource() ==
//                            CameraCapturer.CameraSource.FRONT_CAMERA,
//                    isNetworkQualityEnabled());

//            localParticipant?.setListener(
//                    LocalParticipantListener (
//                            participantController.getThumb(localParticipantSid,
//                                    cameraVideoTrack)));
//            participantController.getThumb(localParticipantSid,
//                    cameraVideoTrack).callOnClick();
//
//            // add existing room participants thumbs
//            boolean isFirstParticipant = true;
//            for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
//            addParticipant(remoteParticipant, isFirstParticipant);
//            isFirstParticipant = false;
//            if (room.getDominantSpeaker() != null) {
//                if (room.getDominantSpeaker().getSid().equals(remoteParticipant.getSid())) {
//                    VideoTrack videoTrack =
//                    (remoteParticipant.getRemoteVideoTracks().size() > 0)
//                    ? remoteParticipant
//                            .getRemoteVideoTracks()
//                            .get(0)
//                            .getRemoteVideoTrack()
//                    : null;
//                    if (videoTrack != null) {
//                        ParticipantView participantView =
//                        participantController.getThumb(
//                                remoteParticipant.getSid(), videoTrack);
//                        participantController.setDominantSpeaker(participantView);
//                    }
//                }
//            }
//        }
        }
    }

    private fun setAudioFocus(setFocus: Boolean) {
        if (setFocus) {
            savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn
            savedIsMicrophoneMute = audioManager.isMicrophoneMute
            unMuteMicrophone()
            savedAudioMode = audioManager.mode
            // Request audio focus before making any device switch.
            requestAudioFocus()
            /*
             * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
             * required to be in this mode when playout and/or recording starts for
             * best possible VoIP performance.
             * Some devices have difficulties with speaker mode if this is not set.
             */
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            updateState { it.copy(
                    volumeControl = true)
            }
        } else {
            audioManager.mode = savedAudioMode
            audioManager.abandonAudioFocus(null)
            audioManager.isMicrophoneMute = savedIsMicrophoneMute
            audioManager.isSpeakerphoneOn = savedIsSpeakerPhoneOn
            updateState { it.copy(
                    volumeControl = false,
                    volumeControlStream = savedVolumeControlStream)
            }
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes =
                    AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
            val focusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener {}
                            .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(
                    null, AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    //    private void switchCamera() {
//        if (cameraCapturer != null) {
//
//            boolean mirror =
//                    cameraCapturer.getCameraSource() ==
// CameraCapturer.CameraSource.BACK_CAMERA;
//
//            cameraCapturer.switchCamera();
//
//            if (participantController.getPrimaryItem().sid.equals(localParticipantSid)) {
//                participantController.updatePrimaryThumb(mirror);
//            } else {
//                participantController.updateThumb(localParticipantSid, cameraVideoTrack,
// mirror);
//            }
//        }
//    }
//
//    private void requestAudioFocus() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            AudioAttributes playbackAttributes =
//                    new AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build();
//            AudioFocusRequest focusRequest =
//                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                            .setAudioAttributes(playbackAttributes)
//                            .setAcceptsDelayedFocusGain(true)
//                            .setOnAudioFocusChangeListener(i -> {})
//                            .build();
//            audioManager.requestAudioFocus(focusRequest);
//        } else {
//            audioManager.requestAudioFocus(
//                    null, AudioManager.STREAM_VOICE_CALL,
// AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//        }
//    }
//

    private fun unMuteMicrophone() {
        val wasMuted = audioManager.isMicrophoneMute
        if (!wasMuted) {
            return
        }
        audioManager.isMicrophoneMute = false
    }

//    private class LocalParticipantListener internal constructor(primaryView: ParticipantView) : LocalParticipant.Listener {
//        private val networkQualityImage: ImageView
//        override fun onAudioTrackPublished(
//                localParticipant: LocalParticipant,
//                localAudioTrackPublication: LocalAudioTrackPublication) {
//        }
//
//        override fun onAudioTrackPublicationFailed(
//                localParticipant: LocalParticipant,
//                localAudioTrack: LocalAudioTrack,
//                twilioException: TwilioException) {
//        }
//
//        override fun onVideoTrackPublished(
//                localParticipant: LocalParticipant,
//                localVideoTrackPublication: LocalVideoTrackPublication) {
//        }
//
//        override fun onVideoTrackPublicationFailed(
//                localParticipant: LocalParticipant,
//                localVideoTrack: LocalVideoTrack,
//                twilioException: TwilioException) {
//        }
//
//        override fun onDataTrackPublished(
//                localParticipant: LocalParticipant,
//                localDataTrackPublication: LocalDataTrackPublication) {
//        }
//
//        override fun onDataTrackPublicationFailed(
//                localParticipant: LocalParticipant,
//                localDataTrack: LocalDataTrack,
//                twilioException: TwilioException) {
//        }
//
//        override fun onNetworkQualityLevelChanged(
//                localParticipant: LocalParticipant,
//                networkQualityLevel: NetworkQualityLevel) {
//            if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
//                    || networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_0)
//            } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_1)
//            } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_2)
//            } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_3)
//            } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_4)
//            } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE) {
//                networkQualityImage.setImageResource(R.drawable.network_quality_level_5)
//            }
//        }
//
//        init {
//            networkQualityImage = primaryView.networkQualityLevelImg
//        }
//    }

    private fun updateState(action: (oldState: RoomViewState) -> RoomViewState) {
        val oldState = mutableViewState.value
        oldState?.let {
            val newState = action(oldState)
            Timber.d("ViewState: $newState")
            mutableViewState.value = newState
        }
    }

    private fun viewEffect(roomViewEffect: RoomViewEffect) {
        Timber.d("ViewEffect: $roomViewEffect")
        mutableViewEffects.value = roomViewEffect
    }

    private fun withState(action: (viewState: RoomViewState) -> Unit) {
        mutableViewState.value?.let {
            action(it)
        }
    }
}