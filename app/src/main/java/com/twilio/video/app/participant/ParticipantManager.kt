package com.twilio.video.app.participant

import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participants: List<ParticipantViewState> get() = mutableParticipants.toList()

    fun updateParticipants(participantViewState: ParticipantViewState) {
        mutableParticipants.find { it.sid == participantViewState.sid }?.let { existingParticipant ->
            mutableParticipants.remove(existingParticipant)
        }
        mutableParticipants.add(participantViewState)
        Timber.d("Participant views: $participants")
    }

    fun clearParticipants() = mutableParticipants.clear()
}