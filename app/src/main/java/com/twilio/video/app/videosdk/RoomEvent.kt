package com.twilio.video.app.videosdk

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room

sealed class RoomEvent {
    data class Connecting(val room: Room): RoomEvent()
    data class Connected(val room: Room): RoomEvent()
    data class Reconnected(val room: Room): RoomEvent()
    data class Reconnecting(val room: Room): RoomEvent()
    data class ParticipantConnected(val participant: RemoteParticipant): RoomEvent()
    data class ParticipantDisconnected(val participant: RemoteParticipant): RoomEvent()
    object ExitRoom() : RoomEvent()
    object ConnectFailure: RoomEvent()
    object MuteLocalAudio: RoomEvent()
    object UnmuteLocalAudio: RoomEvent()
    object Disconnected: RoomEvent()
    object ScreenShareError: RoomEvent()
}