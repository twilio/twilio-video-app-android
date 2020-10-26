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
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.NetworkQualityLevelChange
import timber.log.Timber

class LocalParticipantListener(private val roomManager: RoomManager) : LocalParticipant.Listener {

    override fun onNetworkQualityLevelChanged(localParticipant: LocalParticipant, networkQualityLevel: NetworkQualityLevel) {
        Timber.i("LocalParticipant NetworkQualityLevel changed for LocalParticipant sid: %s, NetworkQualityLevel: %s",
                localParticipant.sid, networkQualityLevel)

        roomManager.sendRoomEvent(NetworkQualityLevelChange(localParticipant.sid, networkQualityLevel))
    }

    override fun onVideoTrackPublished(localParticipant: LocalParticipant, localVideoTrackPublication: LocalVideoTrackPublication) {}

    override fun onVideoTrackPublicationFailed(localParticipant: LocalParticipant, localVideoTrack: LocalVideoTrack, twilioException: TwilioException) {}

    override fun onDataTrackPublished(localParticipant: LocalParticipant, localDataTrackPublication: LocalDataTrackPublication) {}

    override fun onDataTrackPublicationFailed(localParticipant: LocalParticipant, localDataTrack: LocalDataTrack, twilioException: TwilioException) {}

    override fun onAudioTrackPublished(localParticipant: LocalParticipant, localAudioTrackPublication: LocalAudioTrackPublication) {}

    override fun onAudioTrackPublicationFailed(localParticipant: LocalParticipant, localAudioTrack: LocalAudioTrack, twilioException: TwilioException) {}
}
