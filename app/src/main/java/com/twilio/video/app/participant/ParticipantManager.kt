package com.twilio.video.app.participant

import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participantThumbnails: List<ParticipantViewState> get() = mutableParticipants.toList()
    var primaryParticipant: ParticipantViewState? = null
        private set

    fun updateParticipant(participantViewState: ParticipantViewState) {
        Timber.d("Updating participant: %s", participantViewState)
        removeParticipant(participantViewState, false)
        mutableParticipants.add(participantViewState)
        updatePrimaryParticipant()
        Timber.d("Participant thumbnails: $participantThumbnails")
    }

    fun removeParticipant(
        participantViewState: ParticipantViewState,
        updatePrimaryParticipant: Boolean = true
    ) {

        Timber.d("Removing participant: %s", participantViewState.identity)
        mutableParticipants.removeAll { it.sid == participantViewState.sid }
        if (updatePrimaryParticipant) updatePrimaryParticipant()
        Timber.d("Participant thumbnails: $participantThumbnails")
    }

    fun getParticipant(sid: String): ParticipantViewState? = mutableParticipants.find { it.sid == sid }

    fun changeDominantSpeaker(newDominantSpeakerSid: String) {
        mutableParticipants.find { it.isDominantSpeaker }?.copy(
                isDominantSpeaker = false)?.let { oldDominantSpeaker ->
            updateParticipant(oldDominantSpeaker)
        }

        getParticipant(newDominantSpeakerSid)?.copy(
                isDominantSpeaker = true)?.let { updateParticipant(it) }
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