package com.twilio.video.app.sdk

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteAudioTrackPublication
import com.twilio.video.RemoteDataTrack
import com.twilio.video.RemoteDataTrackPublication
import com.twilio.video.RemoteParticipant
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.RemoteVideoTrackPublication
import com.twilio.video.TwilioException
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.MuteRemoteParticipant
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.NetworkQualityLevelChange
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.ScreenTrackUpdated
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.TrackSwitchOff
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.VideoTrackUpdated
import timber.log.Timber

class RemoteParticipantListener(private val roomManager: RoomManager) : RemoteParticipant.Listener {

    override fun onVideoTrackSwitchedOff(remoteParticipant: RemoteParticipant, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("RemoteVideoTrack switched off for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        roomManager.sendRoomEvent(TrackSwitchOff(remoteParticipant.sid, remoteVideoTrack,
                true))
    }

    override fun onVideoTrackSwitchedOn(remoteParticipant: RemoteParticipant, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("RemoteVideoTrack switched on for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        roomManager.sendRoomEvent(TrackSwitchOff(remoteParticipant.sid, remoteVideoTrack,
                false))
    }

    override fun onVideoTrackSubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("RemoteVideoTrack subscribed for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        if (remoteVideoTrack.name.contains(SCREEN_TRACK_NAME))
            roomManager.sendRoomEvent(ScreenTrackUpdated(remoteParticipant.sid, remoteVideoTrack))
        else
            roomManager.sendRoomEvent(VideoTrackUpdated(remoteParticipant.sid, remoteVideoTrack))
    }

    override fun onVideoTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("RemoteVideoTrack unsubscribed for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        if (remoteVideoTrack.name.contains(SCREEN_TRACK_NAME))
            roomManager.sendRoomEvent(ScreenTrackUpdated(remoteParticipant.sid, null))
        else
            roomManager.sendRoomEvent(VideoTrackUpdated(remoteParticipant.sid, null))
    }

    override fun onNetworkQualityLevelChanged(remoteParticipant: RemoteParticipant, networkQualityLevel: NetworkQualityLevel) {
        Timber.i("RemoteParticipant NetworkQualityLevel changed for RemoteParticipant sid: %s, NetworkQualityLevel: %s",
                remoteParticipant.sid, networkQualityLevel)

        roomManager.sendRoomEvent(NetworkQualityLevelChange(remoteParticipant.sid,
                networkQualityLevel))
    }

    override fun onAudioTrackSubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
        Timber.i("RemoteParticipant AudioTrack subscribed for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteAudioTrack.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, false))
    }

    override fun onAudioTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
        Timber.i("RemoteParticipant AudioTrack unsubscribed for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteAudioTrack.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, true))
    }

    override fun onAudioTrackPublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {
        Timber.i("RemoteParticipant AudioTrack published for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteParticipant.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, false))
    }

    override fun onAudioTrackUnpublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {
        Timber.i("RemoteParticipant AudioTrack unpublished for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteParticipant.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, true))
    }

    override fun onAudioTrackEnabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {
        Timber.i("RemoteParticipant AudioTrack enabled for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteParticipant.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, false))
    }

    override fun onAudioTrackDisabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {
        Timber.i("RemoteParticipant AudioTrack disabled for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteParticipant.sid)

        roomManager.sendRoomEvent(MuteRemoteParticipant(remoteParticipant.sid, true))
    }

    override fun onDataTrackPublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}

    override fun onVideoTrackPublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onVideoTrackEnabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onVideoTrackDisabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onDataTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, twilioException: TwilioException) {}

    override fun onDataTrackSubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}

    override fun onVideoTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, twilioException: TwilioException) {}

    override fun onAudioTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, twilioException: TwilioException) {}

    override fun onVideoTrackUnpublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onDataTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}

    override fun onDataTrackUnpublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}
}
