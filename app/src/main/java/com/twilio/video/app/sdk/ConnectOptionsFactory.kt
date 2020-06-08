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
import com.twilio.video.TrackPriority
import com.twilio.video.TrackSwitchOffMode
import com.twilio.video.VideoBandwidthProfileOptions
import com.twilio.video.VideoCodec
import com.twilio.video.VideoDimensions
import com.twilio.video.Vp8Codec
import com.twilio.video.Vp9Codec
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.util.EnvUtil
import com.twilio.video.app.util.get

class ConnectOptionsFactory(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val tokenService: TokenService
) {

    suspend fun newInstance(identity: String, roomName: String): ConnectOptions {

        setSdkEnvironment(sharedPreferences)
        val token = tokenService.getToken(identity, roomName)
        val enableInsights = sharedPreferences.getBoolean(
                Preferences.ENABLE_INSIGHTS,
                Preferences.ENABLE_INSIGHTS_DEFAULT)

        val enableAutomaticTrackSubscription = sharedPreferences.get(
                Preferences.ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION,
                Preferences.ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION_DEFAULT)

        val enableDominantSpeaker = sharedPreferences.get(
                Preferences.ENABLE_DOMINANT_SPEAKER,
                Preferences.ENABLE_DOMINANT_SPEAKER_DEFAULT)

        val preferedVideoCodec: VideoCodec = getVideoCodecPreference(Preferences.VIDEO_CODEC)

        val preferredAudioCodec: AudioCodec = getAudioCodecPreference()

        val configuration = NetworkQualityConfiguration(
                NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL)

        val videoBandwidthProfileOptionsBuilder = VideoBandwidthProfileOptions.Builder()

        sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_MODE,
                Preferences.BANDWIDTH_PROFILE_MODE_DEFAULT).let {
            videoBandwidthProfileOptionsBuilder.mode(getBandwidthProfileMode(it))
        }
        sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE,
                Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT).let {
            videoBandwidthProfileOptionsBuilder.maxSubscriptionBitrate(it.toLong())
        }
        sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS,
                Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT).let {
            videoBandwidthProfileOptionsBuilder.maxTracks(it.toLong())
        }
        sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY,
                Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY_DEFAULT).let {
            videoBandwidthProfileOptionsBuilder.dominantSpeakerPriority(getDominantSpeakerPriority(it))
        }
        sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE,
                Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE_DEFAULT).let {
            videoBandwidthProfileOptionsBuilder.trackSwitchOffMode(getTrackSwitchOffMode(it))
        }
        val renderDimensions = mutableMapOf<TrackPriority, VideoDimensions>()
        setTrackPriorityRenderDimensions(renderDimensions,
                TrackPriority.LOW,
                Preferences.BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS,
                Preferences.BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
        setTrackPriorityRenderDimensions(renderDimensions,
                TrackPriority.STANDARD,
                Preferences.BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS,
                Preferences.BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
        setTrackPriorityRenderDimensions(renderDimensions,
                TrackPriority.HIGH,
                Preferences.BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS,
                Preferences.BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
        videoBandwidthProfileOptionsBuilder.renderDimensions(renderDimensions)
        val bandwidthProfileOptions = BandwidthProfileOptions(videoBandwidthProfileOptionsBuilder.build())

        val isNetworkQualityEnabled = sharedPreferences.get(
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL,
                Preferences.ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT)

        val connectOptionsBuilder = ConnectOptions.Builder(token)
                .roomName(roomName)
                .enableInsights(enableInsights)
                .enableAutomaticSubscription(enableAutomaticTrackSubscription)
                .enableDominantSpeaker(enableDominantSpeaker)
                .enableNetworkQuality(isNetworkQualityEnabled)
                .networkQualityConfiguration(configuration)
                .bandwidthProfile(bandwidthProfileOptions)

        val maxVideoBitrate = sharedPreferences.get(
                Preferences.MAX_VIDEO_BITRATE,
                Preferences.MAX_VIDEO_BITRATE_DEFAULT)

        val maxAudioBitrate = sharedPreferences.get(
                Preferences.MAX_AUDIO_BITRATE,
                Preferences.MAX_AUDIO_BITRATE_DEFAULT)
        connectOptionsBuilder.encodingParameters(EncodingParameters(maxAudioBitrate, maxVideoBitrate))
        connectOptionsBuilder.preferVideoCodecs(listOf(preferedVideoCodec))
        connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))

        return connectOptionsBuilder.build()
    }

    /*
     * Utility method that extracts the VideoDimensions from a preference string in the format
     * NxN. The resolution will be extracted and set to the render dimensions of the specified
     * track priority. If the preference value does match the NxN format, then no render
     * dimenions will be set for the track priority.
     */
    private fun setTrackPriorityRenderDimensions(
        renderDimensions: MutableMap<TrackPriority, VideoDimensions>,
        trackPriority: TrackPriority,
        preferenceKey: String,
        preferenceDefaultValue: String
    ) {
        sharedPreferences.get(preferenceKey, preferenceDefaultValue).let {
            Regex("(\\d+)x(\\d+)").find(it)?.let { match ->
                val (width, height) = match.destructured
                renderDimensions[trackPriority] = VideoDimensions(width.toInt(), height.toInt())
            }
        }
    }

    private fun getTrackSwitchOffMode(trackSwitchOffModeString: String): TrackSwitchOffMode? {
        return when (trackSwitchOffModeString) {
            TrackSwitchOffMode.PREDICTED.name -> TrackSwitchOffMode.PREDICTED
            TrackSwitchOffMode.DETECTED.name -> TrackSwitchOffMode.DETECTED
            TrackSwitchOffMode.DISABLED.name -> TrackSwitchOffMode.DISABLED
            else -> null
        }
    }

    private fun getDominantSpeakerPriority(dominantSpeakerPriorityString: String): TrackPriority? {
        return when (dominantSpeakerPriorityString) {
            TrackPriority.LOW.name -> TrackPriority.LOW
            TrackPriority.STANDARD.name -> TrackPriority.STANDARD
            TrackPriority.HIGH.name -> TrackPriority.HIGH
            else -> null
        }
    }

    private fun getBandwidthProfileMode(modeString: String): BandwidthProfileMode? {
        return when (modeString) {
            BandwidthProfileMode.COLLABORATION.name -> BandwidthProfileMode.COLLABORATION
            BandwidthProfileMode.GRID.name -> BandwidthProfileMode.GRID
            BandwidthProfileMode.PRESENTATION.name -> BandwidthProfileMode.PRESENTATION
            else -> null
        }
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
