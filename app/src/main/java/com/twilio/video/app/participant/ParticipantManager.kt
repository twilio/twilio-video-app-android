package com.twilio.video.app.participant

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.TrackPriority.HIGH
import com.twilio.video.app.sdk.VideoTrackViewState
import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participantThumbnails: List<ParticipantViewState> get() = mutableParticipants.toList()
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

    fun updateParticipantVideoTrack(sid: String, videoTrack: VideoTrackViewState?) {
        mutableParticipants.find { it.sid == sid }?.copy(
                videoTrack = videoTrack)?.let { updateParticipant(it) }
    }

    fun updateParticipantScreenTrack(sid: String, screenTrack: VideoTrackViewState?) {
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
        Timber.d("new dominant speaker with sid: %s", newDominantSpeakerSid)
        newDominantSpeakerSid?.let { sid ->
            clearDominantSpeaker()

            getParticipant(newDominantSpeakerSid)?.copy(
                    isDominantSpeaker = true)?.let { moveDominantSpeakerToTop(it) }
        } ?: run {
            clearDominantSpeaker()
        }
    }

    private fun moveDominantSpeakerToTop(newDominantSpeaker: ParticipantViewState) {
        if (mutableParticipants.size > 1) {
            mutableParticipants.removeAll { it.sid == newDominantSpeaker.sid }
            mutableParticipants.add(1, newDominantSpeaker)
            updatePrimaryParticipant()
        }
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
        primaryParticipant = retrievePrimaryParticipant()
        Timber.d("Participant Cache: $mutableParticipants")
        Timber.d("Primary Participant: $primaryParticipant")
    }

    private fun retrievePrimaryParticipant(): ParticipantViewState? =
            determinePrimaryParticipant()?.apply { setTrackPriority(this) }

    private fun determinePrimaryParticipant(): ParticipantViewState? {
        return mutableParticipants.find { it.isPinned }
                ?: mutableParticipants.find { it.isScreenSharing }
                ?: mutableParticipants.find { it.isDominantSpeaker }
                ?: mutableParticipants.find { !it.isLocalParticipant }
    }

    private fun setTrackPriority(participant: ParticipantViewState) {
        when {
            participant.isScreenSharing -> {
                participant.getRemoteScreenTrack()?.priority = HIGH
            }
            participant.isDominantSpeaker -> {
                participant.getRemoteVideoTrack()?.priority = null
            }
            else -> {
                participant.getRemoteVideoTrack()?.priority = HIGH
            }
        }

        clearOldTrackPriorities()
    }

    private fun clearOldTrackPriorities() {
        primaryParticipant?.run {
            getRemoteVideoTrack()?.priority = null
            getRemoteScreenTrack()?.priority = null
        }
    }
}