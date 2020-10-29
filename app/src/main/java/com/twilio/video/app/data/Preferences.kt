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

import com.twilio.video.BandwidthProfileMode
import com.twilio.video.OpusCodec
import com.twilio.video.TrackPriority
import com.twilio.video.VideoDimensions
import com.twilio.video.Vp8Codec
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.data.api.model.Topology

object Preferences {
    const val INTERNAL = "pref_internal"
    const val SERVER_DEFAULT = "Server Default"
    const val EMAIL = "pref_email"
    const val DISPLAY_NAME = "pref_display_name"
    const val ENVIRONMENT = "pref_environment"
    const val ENVIRONMENT_DEFAULT = BuildConfig.ENVIRONMENT_DEFAULT
    const val TOPOLOGY = "pref_topology"
    val TOPOLOGY_DEFAULT: String = Topology.GROUP.value
    val VIDEO_DIMENSIONS = arrayOf(
            VideoDimensions.CIF_VIDEO_DIMENSIONS,
            VideoDimensions.VGA_VIDEO_DIMENSIONS,
            VideoDimensions.WVGA_VIDEO_DIMENSIONS,
            VideoDimensions.HD_540P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_720P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_960P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_S1080P_VIDEO_DIMENSIONS,
            VideoDimensions.HD_1080P_VIDEO_DIMENSIONS
    )
    const val VIDEO_CAPTURE_RESOLUTION = "pref_video_capture_resolution"
    const val VIDEO_CAPTURE_RESOLUTION_DEFAULT = "1"
    const val VERSION_NAME = "pref_version_name"
    const val VIDEO_LIBRARY_VERSION = "pref_video_library_version"
    const val LOGOUT = "pref_logout"
    const val ENABLE_STATS = "pref_enable_stats"
    const val ENABLE_STATS_DEFAULT = true
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
    const val RECORD_PARTICIPANTS_ON_CONNECT = "pref_record_participants_on_connect"
    const val RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT = false
    const val BANDWIDTH_PROFILE_MODE = "pref_bandwidth_profile_mode"
    val BANDWIDTH_PROFILE_MODE_DEFAULT = BandwidthProfileMode.COLLABORATION.name
    const val BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE = "pref_bandwidth_profile_max_subscription_bitrate"
    const val BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT = 2400
    const val BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS = "pref_bandwidth_profile_max_video_tracks"
    const val BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT = 5
    const val BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY = "pref_bandwidth_profile_dominant_speaker_priority"
    val BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY_DEFAULT = TrackPriority.STANDARD.name
    const val BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE = "pref_bandwidth_profile_track_switch_off_mode"
    const val BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE_DEFAULT = SERVER_DEFAULT
    const val BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS = "pref_bandwidth_profile_low_track_priority_dimensions"
    const val BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT = SERVER_DEFAULT
    const val BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS = "pref_bandwidth_profile_standard_track_priority_dimensions"
    const val BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT = SERVER_DEFAULT
    const val BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS = "pref_bandwidth_profile_high_track_priority_dimensions"
    const val BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT = SERVER_DEFAULT
}
