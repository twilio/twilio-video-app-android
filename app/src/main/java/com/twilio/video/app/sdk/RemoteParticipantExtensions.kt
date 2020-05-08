package com.twilio.video.app.sdk

import com.twilio.video.RemoteParticipant
import com.twilio.video.RemoteVideoTrack

fun RemoteParticipant.getFirstVideoTrack(): RemoteVideoTrack? =
        remoteVideoTracks.firstOrNull()?.remoteVideoTrack
