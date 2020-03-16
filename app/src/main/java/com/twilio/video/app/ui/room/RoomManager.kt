package com.twilio.video.app.ui.room

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twilio.androidenv.Env
import com.twilio.video.AudioCodec
import com.twilio.video.ConnectOptions
import com.twilio.video.EncodingParameters
import com.twilio.video.G722Codec
import com.twilio.video.H264Codec
import com.twilio.video.IsacCodec
import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalVideoTrack
import com.twilio.video.NetworkQualityConfiguration
import com.twilio.video.NetworkQualityVerbosity
import com.twilio.video.OpusCodec
import com.twilio.video.PcmaCodec
import com.twilio.video.PcmuCodec
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.TwilioException
import com.twilio.video.Video
import com.twilio.video.VideoCodec
import com.twilio.video.Vp8Codec
import com.twilio.video.Vp9Codec
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.Connecting
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomEvent.ParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.ParticipantDisconnected
import com.twilio.video.app.ui.room.RoomEvent.RoomState
import com.twilio.video.app.ui.room.RoomEvent.TokenError
import com.twilio.video.app.util.EnvUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RoomManager(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val tokenService: TokenService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    var room: Room? = null
    private val mutableViewEvents: MutableLiveData<RoomEvent?> = MutableLiveData()

    val viewEvents: LiveData<RoomEvent?> = mutableViewEvents

    private val roomListener = RoomListener()

    fun disconnect() {
        room?.disconnect()
    }

    suspend fun connectToRoom(
        identity: String,
        roomName: String,
        localAudioTracks: List<LocalAudioTrack>,
        localVideoTracks: List<LocalVideoTrack>,
        isNetworkQualityEnabled: Boolean
    ) {
        coroutineScope.launch {
            try {
                setSdkEnvironment(sharedPreferences)
                val token = tokenService.getToken(identity, roomName)
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

                val configuration = NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL)

                val connectOptionsBuilder = ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableAutomaticSubscription(enableAutomaticTrackSubscription)
                        .enableDominantSpeaker(enableDominantSpeaker)
                        .enableInsights(enableInsights)
                        .enableNetworkQuality(isNetworkQualityEnabled)
                        .networkQualityConfiguration(configuration)

                val maxVideoBitrate = sharedPreferences.getInt(
                        Preferences.MAX_VIDEO_BITRATE,
                        Preferences.MAX_VIDEO_BITRATE_DEFAULT)

                val maxAudioBitrate = sharedPreferences.getInt(
                        Preferences.MAX_AUDIO_BITRATE,
                        Preferences.MAX_AUDIO_BITRATE_DEFAULT)

                val encodingParameters = EncodingParameters(maxAudioBitrate, maxVideoBitrate)

                if (localAudioTracks.isNotEmpty()) {
                    connectOptionsBuilder.audioTracks(localAudioTracks)
                }

                if (localVideoTracks.isNotEmpty()) {
                    connectOptionsBuilder.videoTracks(localVideoTracks)
                }

                connectOptionsBuilder.preferVideoCodecs(listOf(preferedVideoCodec))
                connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))
                connectOptionsBuilder.encodingParameters(encodingParameters)

                val room = Video.connect(
                        context,
                        connectOptionsBuilder.build(),
                        roomListener)
                this@RoomManager.room = room

                mutableViewEvents.postValue(Connecting(room))
            } catch (e: Exception) {
                Timber.e(e, "Failed to retrieve token")
                mutableViewEvents.postValue(TokenError)
            }
        }
    }

    private fun getPreferenceByKeyWithDefault(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun getVideoCodecPreference(key: String): VideoCodec {
        return sharedPreferences.getString(key, Preferences.VIDEO_CODEC_DEFAULT)?.let { videoCodecName ->
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
            } ?: Vp8Codec()
    }

    private fun getAudioCodecPreference(): AudioCodec {
        return sharedPreferences.getString(
                Preferences.AUDIO_CODEC, Preferences.AUDIO_CODEC_DEFAULT)?.let { audioCodecName ->

            when (audioCodecName) {
                IsacCodec.NAME -> IsacCodec()
                PcmaCodec.NAME -> PcmaCodec()
                PcmuCodec.NAME -> PcmuCodec()
                G722Codec.NAME -> G722Codec()
                else -> OpusCodec()
            }
        } ?: OpusCodec()
    }

    private fun setSdkEnvironment(sharedPreferences: SharedPreferences) {
        val env = sharedPreferences.getString(
                Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT)
        val nativeEnvironmentVariableValue = EnvUtil.getNativeEnvironmentVariableValue(env)
        Env.set(
                context,
                EnvUtil.TWILIO_ENV_KEY,
                nativeEnvironmentVariableValue,
                true)
    }

    inner class RoomListener : Room.Listener {
        override fun onConnected(room: Room) {
            Timber.i("onConnected -> room sid: %s",
                    room.sid)
            mutableViewEvents.value = RoomState(room)
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Timber.i("Disconnected from room -> sid: %s, state: %s",
                    room.sid, room.state)
            mutableViewEvents.value = RoomState(room)
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Timber.e(
                    "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                    room.sid,
                    room.state,
                    twilioException.code,
                    twilioException.message)
            mutableViewEvents.value = ConnectFailure(room)
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)
            mutableViewEvents.value = ParticipantConnected(room, remoteParticipant)
        }

        override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)
            mutableViewEvents.value = ParticipantDisconnected(room, remoteParticipant)
        }

        override fun onDominantSpeakerChanged(room: Room, remoteParticipant: RemoteParticipant?) {
            Timber.i("DominantSpeakerChanged -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant?.sid)
            mutableViewEvents.value = DominantSpeakerChanged(room, remoteParticipant)
        }

        override fun onRecordingStarted(room: Room) {}

        override fun onReconnected(room: Room) {
            Timber.i("onReconnected: %s", room.name)
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Timber.i("onReconnecting: %s", room.name)
        }

        override fun onRecordingStopped(room: Room) {}
    }
}