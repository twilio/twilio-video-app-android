package com.twilio.video.app.ui.room

import com.twilio.video.app.participant.ParticipantViewState
import timber.log.Timber

internal fun ParticipantController.updateThumbnails(participants: List<ParticipantViewState>?) {
    participants?.let {
        // Add or update any participants in the thumbs list
        participants.forEach { participant ->
            if (getThumb(participant.sid) == null) addThumb(participant) else updateThumb(participant)
        }

        // Delete any thumbs that aren't in the participant list
        var thumbnailLog = StringBuilder()
        thumbs.keys.forEach { thumb ->
            thumbnailLog.append(thumb.identity).append(" ")
            if (participants.find { it.sid == thumb.sid } == null) {
                removeThumb(thumb.sid)
            }
        }
        Timber.d("Thumbnails updated: %s", thumbnailLog)
    } ?: removeAllThumbs()
}