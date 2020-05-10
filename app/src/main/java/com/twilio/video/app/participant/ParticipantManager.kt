package com.twilio.video.app.participant

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.VideoTrack
import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participantThumbnails: List<ParticipantViewState> get() = mutableParticipants.toList()
    var primaryParticipant: ParticipantViewState? = null
        private set

    fun updateParticipant(participantViewState: ParticipantViewState) {
        Timber.d("Updating participant: %s", participantViewState)
        removeParticipant(participantViewState.sid, false)
        mutableParticipants.add(participantViewState)
        updatePrimaryParticipant()
        Timber.d("Participant thumbnails: $participantThumbnails")
    }

    fun removeParticipant(sid: String, updatePrimaryParticipant: Boolean = true) {
        Timber.d("Removing participant: %s", sid)
        mutableParticipants.removeAll { it.sid == sid }
        if (updatePrimaryParticipant) updatePrimaryParticipant()
        Timber.d("Participant thumbnails: $participantThumbnails")
    }

    fun getParticipant(sid: String): ParticipantViewState? = mutableParticipants.find { it.sid == sid }

    fun updateNetworkQuality(sid: String, networkQualityLevel: NetworkQualityLevel) {
        getParticipant(sid)?.copy(networkQualityLevel = networkQualityLevel)?.let {
            updateParticipant(it)
        }
    }

    fun updateParticipantVideoTrack(sid: String, videoTrack: VideoTrack?) {
        getParticipant(sid)?.copy(videoTrack = videoTrack)?.let {
            updateParticipant(it)
        }
    }

    fun muteParticipant(sid: String, mute: Boolean) {
        getParticipant(sid)?.copy(isMuted = mute)?.let {
            updateParticipant(it)
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
                isDominantSpeaker = false)?.let { oldDominantSpeaker ->
            updateParticipant(oldDominantSpeaker)
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