package com.twilio.video.app.sdk

import android.content.Context
import com.twilio.video.Participant
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.Room.State.DISCONNECTED
import com.twilio.video.TwilioException
import com.twilio.video.TwilioException.ROOM_MAX_PARTICIPANTS_EXCEEDED_EXCEPTION
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.data.api.AuthServiceException
import com.twilio.video.app.ui.room.RoomEvent
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.Connected
import com.twilio.video.app.ui.room.RoomEvent.Connecting
import com.twilio.video.app.ui.room.RoomEvent.Disconnected
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomEvent.MaxParticipantFailure
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.ParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.ParticipantEvent.ParticipantDisconnected
import com.twilio.video.app.ui.room.VideoService.Companion.startService
import com.twilio.video.app.ui.room.VideoService.Companion.stopService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

const val MICROPHONE_TRACK_NAME = "microphone"
const val CAMERA_TRACK_NAME = "camera"
const val SCREEN_TRACK_NAME = "screen"

class RoomManager(
    private val context: Context,
    private val videoClient: VideoClient
) {

    private val roomListener = RoomListener()
    private var roomScope: CoroutineScope? = null
    private var roomChannel: Channel<RoomEvent>? = null
    var room: Room? = null

    fun disconnect() {
        room?.disconnect()
    }

    suspend fun connect(identity: String, roomName: String): ReceiveChannel<RoomEvent> {
        setupChannel()
        sendToChannel(Connecting)

        room = try {
            videoClient.connect(identity, roomName, roomListener)
        } catch (e: AuthServiceException) {
            handleTokenException(e, e.error)
        } catch (e: Exception) {
            handleTokenException(e)
        } finally {
            setupChannelShutdownListener()
        }

        return roomChannel as ReceiveChannel<RoomEvent>
    }

    private fun setupChannel() {
        check(roomScope == null && roomChannel == null)
        roomScope = CoroutineScope(Dispatchers.IO)
        roomChannel = Channel(Channel.BUFFERED)
        Timber.d("Setup new Room Coroutine Scope and Channel: \n$roomScope\n$roomChannel")
    }

    private fun sendToChannel(roomEvent: RoomEvent) {
        roomScope?.launch { roomChannel?.send(roomEvent) }
    }

    fun sendRoomEvent(roomEvent: RoomEvent) {
        sendToChannel(roomEvent)
    }

    private fun handleTokenException(e: Exception, error: AuthServiceError? = null): Room? {
        Timber.e(e, "Failed to retrieve token")
        sendToChannel(RoomEvent.TokenError(serviceError = error))
        return null
    }

    private fun shutdownRoom() {
        Timber.d("Shutting down Room Coroutine Scope and Channel: \n$roomScope\n$roomChannel")
        roomScope?.cancel()
        roomChannel?.close()
        roomScope = null
        roomChannel = null
    }

    @ExperimentalCoroutinesApi
    private fun setupChannelShutdownListener() {
        roomScope?.launch {
            while (isActive) {
                if ((roomChannel as ReceiveChannel<RoomEvent>).isEmpty &&
                        (room == null || room?.state == DISCONNECTED)) {
                    shutdownRoom()
                }
                delay(500)
            }
        }
    }

    inner class RoomListener : Room.Listener {
        override fun onConnected(room: Room) {
            Timber.i("onConnected -> room sid: %s",
                    room.sid)

            startService(context, room.name)

            setupParticipants(room)
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Timber.i("Disconnected from room -> sid: %s, state: %s",
                    room.sid, room.state)

            stopService(context)

            sendToChannel(Disconnected)
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Timber.e(
                    "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                    room.sid,
                    room.state,
                    twilioException.code,
                    twilioException.message)

            if (twilioException.code == ROOM_MAX_PARTICIPANTS_EXCEEDED_EXCEPTION) {
                sendRoomEvent(MaxParticipantFailure)
            } else {
                sendRoomEvent(ConnectFailure)
            }
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)

            remoteParticipant.setListener(RemoteParticipantListener(this@RoomManager))
            sendRoomEvent(ParticipantConnected(remoteParticipant))
        }

        override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)

            sendRoomEvent(ParticipantDisconnected(remoteParticipant.sid))
        }

        override fun onDominantSpeakerChanged(room: Room, remoteParticipant: RemoteParticipant?) {
            Timber.i("DominantSpeakerChanged -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant?.sid)

            sendToChannel(DominantSpeakerChanged(remoteParticipant?.sid))
        }

        override fun onRecordingStarted(room: Room) {}

        override fun onReconnected(room: Room) {
            Timber.i("onReconnected: %s", room.name)
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Timber.i("onReconnecting: %s", room.name)
        }

        override fun onRecordingStopped(room: Room) {}

        private fun setupParticipants(room: Room) {
            room.localParticipant?.let { localParticipant ->
                val participants = mutableListOf<Participant>()
                participants.add(localParticipant)
                localParticipant.setListener(LocalParticipantListener(this@RoomManager))

                room.remoteParticipants.forEach {
                    it.setListener(RemoteParticipantListener(this@RoomManager))
                    participants.add(it)
                }

                sendToChannel(Connected(participants, room, room.name))
            }
        }
    }
}
