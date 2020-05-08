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
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.ui.room.RoomManager
import timber.log.Timber

class RemoteParticipantListener(
    private val roomManager: RoomManager,
    private val participantManager: ParticipantManager
) : RemoteParticipant.Listener {

    override fun onVideoTrackSubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("New RemoteParticipant RemoteVideoTrack published for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        getCachedParticipant(remoteParticipant)?.copy(
            videoTrack = remoteParticipant.getFirstVideoTrack()
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onVideoTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, remoteVideoTrack: RemoteVideoTrack) {
        Timber.i("RemoteParticipant RemoteVideoTrack unpublished for RemoteParticipant sid: %s, RemoteVideoTrack sid: %s",
                remoteParticipant.sid, remoteVideoTrack.sid)

        getCachedParticipant(remoteParticipant)?.copy(
            videoTrack = null
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onNetworkQualityLevelChanged(remoteParticipant: RemoteParticipant, networkQualityLevel: NetworkQualityLevel) {
        Timber.i("RemoteParticipant NetworkQualityLevel changed for RemoteParticipant sid: %s, NetworkQualityLevel: %s",
                remoteParticipant.sid, networkQualityLevel)

        getCachedParticipant(remoteParticipant)?.copy(
                networkQualityLevel = networkQualityLevel
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onAudioTrackSubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
        Timber.i("RemoteParticipant AudioTrack subscribed for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteAudioTrack.sid)

        getCachedParticipant(remoteParticipant)?.copy(
                muted = false
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onAudioTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, remoteAudioTrack: RemoteAudioTrack) {
        Timber.i("RemoteParticipant AudioTrack unsubscribed for RemoteParticipant sid: %s, RemoteAudioTrack sid: %s",
                remoteParticipant.sid, remoteAudioTrack.sid)

        getCachedParticipant(remoteParticipant)?.copy(
                muted = true
        )?.let {
            roomManager.updateParticipant(it)
        }
    }

    override fun onDataTrackPublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}

    override fun onAudioTrackEnabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}

    override fun onAudioTrackPublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}

    override fun onVideoTrackPublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onVideoTrackEnabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onVideoTrackDisabled(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onDataTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, twilioException: TwilioException) {}

    override fun onAudioTrackDisabled(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}

    override fun onDataTrackSubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}

    override fun onVideoTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication, twilioException: TwilioException) {}

    override fun onAudioTrackSubscriptionFailed(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication, twilioException: TwilioException) {}

    override fun onAudioTrackUnpublished(remoteParticipant: RemoteParticipant, remoteAudioTrackPublication: RemoteAudioTrackPublication) {}

    override fun onVideoTrackUnpublished(remoteParticipant: RemoteParticipant, remoteVideoTrackPublication: RemoteVideoTrackPublication) {}

    override fun onDataTrackUnsubscribed(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication, remoteDataTrack: RemoteDataTrack) {}

    override fun onDataTrackUnpublished(remoteParticipant: RemoteParticipant, remoteDataTrackPublication: RemoteDataTrackPublication) {}

    private fun getCachedParticipant(remoteParticipant: RemoteParticipant) =
            participantManager.getParticipant(remoteParticipant.sid)
}