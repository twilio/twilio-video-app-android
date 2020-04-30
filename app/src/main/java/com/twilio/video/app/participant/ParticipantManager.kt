package com.twilio.video.app.participant

class ParticipantManager {

    private val participants = mutableListOf<ParticipantViewState>()

    fun updateParticipants(participantViewState: ParticipantViewState) {
        participants.add(participantViewState)
    }

    fun clearParticipants() = participants.clear()
}