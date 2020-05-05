package com.twilio.video.app.participant

import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.RemoteParticipant
import timber.log.Timber

class ParticipantManager {

    private val mutableParticipants = mutableListOf<ParticipantViewState>()
    val participants: List<ParticipantViewState> get() = mutableParticipants.toList()

    fun updateParticipants(participant: LocalParticipant, videoTrack: LocalVideoTrack) {
        mutableParticipants.add(buildLocalParticipantViewState(participant, videoTrack))
        Timber.d("Participant views: $participants")
    }

    fun updateParticipants(participant: RemoteParticipant) {
        mutableParticipants.find { it.sid == participant.sid }?.let { existingParticipant ->
            mutableParticipants.remove(existingParticipant)
        }
        mutableParticipants.add(buildParticipantViewState(participant))
        Timber.d("Participant views: $participants")
    }

    fun clearParticipants() = mutableParticipants.clear()

    private fun buildParticipantViewState(participant: RemoteParticipant) =
            ParticipantViewState(
                    participant.sid,
                    participant.identity,
                    participant.remoteVideoTracks?.firstOrNull()?.remoteVideoTrack
            )

    private fun buildLocalParticipantViewState(participant: LocalParticipant, videoTrack: LocalVideoTrack) =
            ParticipantViewState(
                    participant.sid,
                    "You",
                    videoTrack,
                    isLocalParticipant = true)
}