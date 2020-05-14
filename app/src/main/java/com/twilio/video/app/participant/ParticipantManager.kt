package com.twilio.video.app.participant

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.VideoTrack
import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participantThumbnails: List<ParticipantViewState> get() = mutableParticipants
    var primaryParticipant: ParticipantViewState? = null
        private set

    fun addParticipant(participantViewState: ParticipantViewState) {
        Timber.d("Adding participant: %s", participantViewState)
        mutableParticipants.add(participantViewState)
        updatePrimaryParticipant()
    }

    fun updateParticipant(participantViewState: ParticipantViewState) {
        mutableParticipants.indexOfFirst { it.sid == participantViewState.sid }.let { index ->
            if (index > -1) {
                Timber.d("Updating participant: %s", participantViewState)
                mutableParticipants[index] = participantViewState
                updatePrimaryParticipant()
            }
        }
    }

    fun removeParticipant(sid: String) {
        Timber.d("Removing participant: %s", sid)
        mutableParticipants.removeAll { it.sid == sid }
        updatePrimaryParticipant()
    }

    fun getParticipant(sid: String): ParticipantViewState? = mutableParticipants.find { it.sid == sid }

    fun updateNetworkQuality(sid: String, networkQualityLevel: NetworkQualityLevel) {
        getParticipant(sid)?.copy(networkQualityLevel = networkQualityLevel)?.let {
            updateParticipant(it)
        }
    }

    fun updateParticipantVideoTrack(sid: String, videoTrack: VideoTrack?) {
        mutableParticipants.find { it.sid == sid }?.copy(
                videoTrack = videoTrack)?.let { updateParticipant(it) }
    }

    fun updateParticipantScreenTrack(sid: String, screenTrack: VideoTrack?) {
        mutableParticipants.find { it.sid == sid }?.copy(
                screenTrack = screenTrack)?.let { updateParticipant(it) }
    }

    fun muteParticipant(sid: String, mute: Boolean) {
        getParticipant(sid)?.copy(isMuted = mute)?.let {
            updateParticipant(it)
        }
    }

    fun changePinnedParticipant(sid: String) {
        val existingPin = mutableParticipants.find { it.isPinned }?.copy(
            isPinned = false)
        existingPin?.let { updateParticipant(it) }

        getParticipant(sid)?.let { newPin ->
            if (existingPin?.sid != newPin.sid) {
                updateParticipant(newPin.copy(isPinned = true))
            }
        }
    }

    fun changeDominantSpeaker(newDominantSpeakerSid: String?) {
        newDominantSpeakerSid?.let {
            clearDominantSpeaker()

            getParticipant(newDominantSpeakerSid)?.copy(
                    isDominantSpeaker = true)?.let { updateParticipant(it) }
        } ?: clearDominantSpeaker()
    }

    private fun clearDominantSpeaker() {
        mutableParticipants.find { it.isDominantSpeaker }?.copy(
                isDominantSpeaker = false)?.let { updateParticipant(it) }
    }

    fun clearParticipants() {
        mutableParticipants.clear()
        primaryParticipant = null
    }

    private fun updatePrimaryParticipant() {
        primaryParticipant = determinePrimaryParticipant()
        Timber.d("Participant Cache: $mutableParticipants")
        Timber.d("Primary Participant: $primaryParticipant")
    }

    private fun determinePrimaryParticipant(): ParticipantViewState? {
        return mutableParticipants.find { it.isPinned }
        ?: mutableParticipants.find { it.screenTrack != null }
        ?: mutableParticipants.find { !it.isLocalParticipant }
    }
}