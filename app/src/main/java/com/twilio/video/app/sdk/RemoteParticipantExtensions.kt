package com.twilio.video.app.sdk

import com.twilio.video.Participant
import com.twilio.video.VideoTrack

fun Participant.getFirstVideoTrack(): VideoTrack? =
        videoTracks.firstOrNull()?.videoTrack
