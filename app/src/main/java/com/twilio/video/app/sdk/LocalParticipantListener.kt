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
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.NetworkQualityLevelChange
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.ScreenTrackUpdated
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.VideoTrackUpdated
import timber.log.Timber

class LocalParticipantListener(private val roomManager: RoomManager) : LocalParticipant.Listener {

    override fun onNetworkQualityLevelChanged(localParticipant: LocalParticipant, networkQualityLevel: NetworkQualityLevel) {
        Timber.i("LocalParticipant NetworkQualityLevel changed for LocalParticipant sid: %s, NetworkQualityLevel: %s",
                localParticipant.sid, networkQualityLevel)

        roomManager.sendParticipantEvent(NetworkQualityLevelChange(localParticipant.sid, networkQualityLevel))
    }

    override fun onVideoTrackPublished(localParticipant: LocalParticipant, localVideoTrackPublication: LocalVideoTrackPublication) {
        Timber.i("New LocalParticipant VideoTrack published for LocalParticipant sid: %s, LocalVideoTrack: %s",
                localParticipant.sid, localVideoTrackPublication.localVideoTrack)

        if (localVideoTrackPublication.videoTrack.name.contains(SCREEN_TRACK_NAME)) {
            roomManager.sendParticipantEvent(ScreenTrackUpdated(localParticipant.sid,
                    localVideoTrackPublication.videoTrack))
        } else {
            roomManager.sendParticipantEvent(VideoTrackUpdated(localParticipant.sid,
                    localVideoTrackPublication.videoTrack))
        }
    }

    override fun onVideoTrackPublicationFailed(localParticipant: LocalParticipant, localVideoTrack: LocalVideoTrack, twilioException: TwilioException) {}

    override fun onDataTrackPublished(localParticipant: LocalParticipant, localDataTrackPublication: LocalDataTrackPublication) {}

    override fun onDataTrackPublicationFailed(localParticipant: LocalParticipant, localDataTrack: LocalDataTrack, twilioException: TwilioException) {}

    override fun onAudioTrackPublished(localParticipant: LocalParticipant, localAudioTrackPublication: LocalAudioTrackPublication) {}

    override fun onAudioTrackPublicationFailed(localParticipant: LocalParticipant, localAudioTrack: LocalAudioTrack, twilioException: TwilioException) {}
}