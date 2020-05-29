/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twilio.video.app.data

import android.content.SharedPreferences
import com.twilio.video.BandwidthProfileMode
import com.twilio.video.OpusCodec
import com.twilio.video.Vp8Codec
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.data.api.model.Topology

object Preferences {
    const val EMAIL = "pref_email"
    const val DISPLAY_NAME = "pref_display_name"
    const val ENVIRONMENT = "pref_environment"
    const val ENVIRONMENT_DEFAULT = BuildConfig.ENVIRONMENT_DEFAULT
    const val TOPOLOGY = "pref_topology"
    val TOPOLOGY_DEFAULT: String = Topology.GROUP.string
    const val MIN_FPS = "pref_min_fps"
    const val MAX_FPS = "pref_max_fps"
    const val MIN_VIDEO_DIMENSIONS = "pref_min_video_dim"
    const val MAX_VIDEO_DIMENSIONS = "pref_max_video_dim"
    const val ASPECT_RATIO = "pref_aspect_ratio"
    const val VERSION_NAME = "pref_version_name"
    const val VERSION_CODE = "pref_version_code"
    const val VIDEO_LIBRARY_VERSION = "pref_video_library_version"
    const val LOGOUT = "pref_logout"
    const val ENABLE_STATS = "pref_enable_stats"
    const val ENABLE_INSIGHTS = "pref_enable_insights"
    const val ENABLE_NETWORK_QUALITY_LEVEL = "pref_enable_network_quality_level"
    const val ENABLE_NETWORK_QUALITY_LEVEL_DEFAULT = true
    const val ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION = "pref_enable_automatic_subscription"
    const val ENABLE_AUTOMATIC_TRACK_SUBSCRIPTION_DEFAULT = true
    const val ENABLE_DOMINANT_SPEAKER = "pref_enable_dominant_speaker"
    const val ENABLE_DOMINANT_SPEAKER_DEFAULT = true
    const val ENABLE_INSIGHTS_DEFAULT = true
    const val VIDEO_CODEC = "pref_video_codecs"
    const val VIDEO_CODEC_DEFAULT = Vp8Codec.NAME
    const val VP8_SIMULCAST = "pref_vp8_simulcast"
    const val VP8_SIMULCAST_DEFAULT = false
    const val AUDIO_CODEC = "pref_audio_codecs"
    const val AUDIO_CODEC_DEFAULT = OpusCodec.NAME
    const val MAX_AUDIO_BITRATE = "pref_max_audio_bitrate"
    const val MAX_AUDIO_BITRATE_DEFAULT = 16
    const val MAX_VIDEO_BITRATE = "pref_max_video_bitrate"
    const val MAX_VIDEO_BITRATE_DEFAULT = 0
    const val MAX_VIDEO_TRACKS = "pref_max_video_tracks"
    const val MAX_VIDEO_TRACKS_DEFAULT = 5L
    const val RECORD_PARTICIPANTS_ON_CONNECT = "pref_record_participants_on_connect"
    const val RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT = false
    const val BANDWIDTH_PROFILE_MODE = "pref_bandwidth_profile_mode"
    val BANDWIDTH_PROFILE_MODE_DEFAULT = BandwidthProfileMode.COLLABORATION.name
    const val BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE = "pref_bandwidth_profile_max_subscription_bitrate"
    const val BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT = 2400
}

/*
 * Utility method that allows getting a shared preference with a default value. The return value
 * type is inferred by the default value type.
 */
inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is Boolean -> getBoolean(key, defaultValue) as T
        is Float -> getFloat(key, defaultValue) as T
        is Int -> getInt(key, defaultValue) as T
        is Long -> getLong(key, defaultValue) as T
        is String -> getString(key, defaultValue) as T
        else -> throw IllegalArgumentException("Attempted to get preference with unsupported type")
    }
}
