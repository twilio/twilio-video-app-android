package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.TwilioException
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.Connected
import com.twilio.video.app.ui.room.RoomEvent.Connecting
import com.twilio.video.app.ui.room.RoomEvent.Disconnected
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomEvent.ParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.ParticipantDisconnected
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

class RoomManager : Room.Listener {

    private var room: Room? = null
    private val mutableViewEvents: MutableLiveData<RoomEvent?> = MutableLiveData()

    val viewEvents: LiveData<RoomEvent?> = mutableViewEvents

    val roomConnectionObserver = object: SingleObserver<Room> {
        override fun onSuccess(room: Room) {
            this@RoomManager.run {
                this.room = room
                mutableViewEvents.value = Connecting(room)
            }
        }

        override fun onError(e: Throwable) {
            Timber.e("%s -> reason: %s", "Failed to retrieve access token", e.message)
        }

        override fun onSubscribe(d: Disposable) {
        }
    }

    override fun onConnected(room: Room) {
        mutableViewEvents.value = Connected(room)
    }

    override fun onDisconnected(room: Room, twilioException: TwilioException?) {
        Timber.i("Disconnected from room -> sid: %s, state: %s",
                room.sid, room.state)
        mutableViewEvents.value = Disconnected(room)
    }

    override fun onConnectFailure(room: Room, twilioException: TwilioException) {
        Timber.e(
                "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                room.sid,
                room.state,
                twilioException.code,
                twilioException.message)
        mutableViewEvents.value = ConnectFailure(room)
    }

    override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
        Timber.i("RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                room.sid, remoteParticipant.sid)
        mutableViewEvents.value = ParticipantConnected(room, remoteParticipant)
    }

    override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
        Timber.i("RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                room.sid, remoteParticipant.sid)
        mutableViewEvents.value = ParticipantDisconnected(room, remoteParticipant)
    }

    override fun onDominantSpeakerChanged(room: Room, remoteParticipant: RemoteParticipant?) {
        Timber.i("DominantSpeakerChanged -> room sid: %s, remoteParticipant: %s",
                room.sid, remoteParticipant?.sid)
        mutableViewEvents.value = DominantSpeakerChanged(room, remoteParticipant)
    }

    override fun onRecordingStarted(room: Room) {}

    override fun onReconnected(room: Room) {
        Timber.i("onReconnected: %s", room.name)
    }

    override fun onReconnecting(room: Room, twilioException: TwilioException) {
        Timber.i("onReconnecting: %s", room.name)
    }

    override fun onRecordingStopped(room: Room) {}

    fun disconnect() {
        room?.disconnect()
    }
}