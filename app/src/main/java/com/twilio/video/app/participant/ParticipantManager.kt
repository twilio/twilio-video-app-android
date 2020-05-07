package com.twilio.video.app.participant

import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participantThumbnails: List<ParticipantViewState> get() = mutableParticipants.toList()
    var primaryParticipant: ParticipantViewState? = null
        private set

    fun updateParticipant(participantViewState: ParticipantViewState) {
        removeParticipant(participantViewState)
        mutableParticipants.add(participantViewState)
        updatePrimaryParticipant()
        Timber.d("Participant thumbnails: $participantThumbnails")
    }

    fun removeParticipant(participantViewState: ParticipantViewState) {
        mutableParticipants.find { it.sid == participantViewState.sid }?.let { existingParticipant ->
            mutableParticipants.remove(existingParticipant)
            updatePrimaryParticipant()
            Timber.d("Participant thumbnails: $participantThumbnails")
        }
    }

    fun clearParticipants() {
        mutableParticipants.clear()
        primaryParticipant = null
    }

    private fun updatePrimaryParticipant() {
        primaryParticipant = mutableParticipants.find { !it.isLocalParticipant }
        Timber.d("Primary Participant: $primaryParticipant")
    }
}