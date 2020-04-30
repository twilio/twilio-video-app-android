package com.twilio.video.app.participant

import com.twilio.video.VideoTrack

data class ParticipantViewState(
    val sid: String? = null,
    val identity: String? = null,
    val videoTrack: VideoTrack? = null,
    val muted: Boolean = false,
    val mirror: Boolean = false,
    val isPinned: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isDominantSpeaker: Boolean = false,
    val isLocalParticipant: Boolean = false
)