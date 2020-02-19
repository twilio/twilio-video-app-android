package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.TwilioException

class RoomManager : Room.Listener {

    override fun onRecordingStopped(room: Room) {
    }

    override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
    }

    override fun onRecordingStarted(room: Room) {
    }

    override fun onConnectFailure(room: Room, twilioException: TwilioException) {
    }

    override fun onReconnected(room: Room) {
    }

    override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
    }

    override fun onConnected(room: Room) {
    }

    override fun onDisconnected(room: Room, twilioException: TwilioException?) {
    }

    override fun onReconnecting(room: Room, twilioException: TwilioException) {
    }
}