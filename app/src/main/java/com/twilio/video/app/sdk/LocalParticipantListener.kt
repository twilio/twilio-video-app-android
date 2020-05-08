package com.twilio.video.app.sdk

import com.twilio.video.LocalAudioTrack
import com.twilio.video.LocalAudioTrackPublication
import com.twilio.video.LocalDataTrack
import com.twilio.video.LocalDataTrackPublication
import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.LocalVideoTrackPublication
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.TwilioException
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.ui.room.RoomManager

class LocalParticipantListener(
    private val roomManager: RoomManager,
    private val participantManager: ParticipantManager
) : LocalParticipant.Listener {

    override fun onNetworkQualityLevelChanged(localParticipant: LocalParticipant, networkQualityLevel: NetworkQualityLevel) {
        participantManager.getParticipant(localParticipant.sid)?.copy(
                networkQualityLevel = networkQualityLevel
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onVideoTrackPublicationFailed(localParticipant: LocalParticipant, localVideoTrack: LocalVideoTrack, twilioException: TwilioException) {}

    override fun onDataTrackPublished(localParticipant: LocalParticipant, localDataTrackPublication: LocalDataTrackPublication) {}

    override fun onDataTrackPublicationFailed(localParticipant: LocalParticipant, localDataTrack: LocalDataTrack, twilioException: TwilioException) {}

    override fun onAudioTrackPublished(localParticipant: LocalParticipant, localAudioTrackPublication: LocalAudioTrackPublication) {}

    override fun onAudioTrackPublicationFailed(localParticipant: LocalParticipant, localAudioTrack: LocalAudioTrack, twilioException: TwilioException) {}

    override fun onVideoTrackPublished(localParticipant: LocalParticipant, localVideoTrackPublication: LocalVideoTrackPublication) {}
}