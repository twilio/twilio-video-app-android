package com.twilio.video.app.sdk

import com.twilio.video.VideoTrack

data class VideoTrackViewState(
    val videoTrack: VideoTrack,
    val isSwitchedOff: Boolean = false
)
