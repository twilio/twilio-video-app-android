package com.twilio.video.app.sdk

import android.content.Context
import android.content.SharedPreferences
import com.twilio.androidenv.Env
import com.twilio.video.AudioCodec
import com.twilio.video.BandwidthProfileMode
import com.twilio.video.BandwidthProfileOptions
import com.twilio.video.ConnectOptions
import com.twilio.video.EncodingParameters
import com.twilio.video.G722Codec
import com.twilio.video.H264Codec
import com.twilio.video.IsacCodec
import com.twilio.video.NetworkQualityConfiguration
import com.twilio.video.NetworkQualityVerbosity
import com.twilio.video.OpusCodec
import com.twilio.video.PcmaCodec
import com.twilio.video.PcmuCodec
import com.twilio.video.Room
import com.twilio.video.TrackPriority
import com.twilio.video.Video
import com.twilio.video.VideoBandwidthProfileOptions
import com.twilio.video.VideoCodec
import com.twilio.video.Vp8Codec
import com.twilio.video.Vp9Codec
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.MAX_VIDEO_TRACKS
import com.twilio.video.app.data.Preferences.MAX_VIDEO_TRACKS_DEFAULT
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.util.EnvUtil

class RoomFactory(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val tokenService: TokenService
) {

    suspend fun newInstance(
        identity: String,
        roomName: String,
        isNetworkQualityEnabled: Boolean,
        roomListener: Room.Listener
    ): Room {

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

            val maxTracks = sharedPreferences.getLong(MAX_VIDEO_TRACKS, MAX_VIDEO_TRACKS_DEFAULT)
            val bandwidthProfileOptions = BandwidthProfileOptions(
                        VideoBandwidthProfileOptions.Builder()
                                .mode(BandwidthProfileMode.COLLABORATION)
                                .maxTracks(maxTracks)
                                .dominantSpeakerPriority(TrackPriority.STANDARD)
                                .build())

            val connectOptionsBuilder = ConnectOptions.Builder(token)
                    .roomName(roomName)
                    .enableAutomaticSubscription(enableAutomaticTrackSubscription)
                    .enableDominantSpeaker(enableDominantSpeaker)
                    .enableInsights(enableInsights)
                    .enableNetworkQuality(isNetworkQualityEnabled)
                    .networkQualityConfiguration(configuration)
                    .bandwidthProfile(bandwidthProfileOptions)

            val maxVideoBitrate = sharedPreferences.getInt(
                    Preferences.MAX_VIDEO_BITRATE,
                    Preferences.MAX_VIDEO_BITRATE_DEFAULT)

            val maxAudioBitrate = sharedPreferences.getInt(
                    Preferences.MAX_AUDIO_BITRATE,
                    Preferences.MAX_AUDIO_BITRATE_DEFAULT)

            val encodingParameters = EncodingParameters(maxAudioBitrate, maxVideoBitrate)

            connectOptionsBuilder.preferVideoCodecs(listOf(preferedVideoCodec))
            connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))
            connectOptionsBuilder.encodingParameters(encodingParameters)

            return Video.connect(
                    context,
                    connectOptionsBuilder.build(),
                    roomListener)
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
}