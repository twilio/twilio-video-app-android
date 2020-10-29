package com.twilio.video.app.sdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.twilio.video.Participant
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.StatsReport
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
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.RemoteParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.RemoteParticipantDisconnected
import com.twilio.video.app.ui.room.RoomEvent.StatsUpdate
import com.twilio.video.app.ui.room.VideoService.Companion.startService
import com.twilio.video.app.ui.room.VideoService.Companion.stopService
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

const val MICROPHONE_TRACK_NAME = "microphone"
const val CAMERA_TRACK_NAME = "camera"
const val SCREEN_TRACK_NAME = "screen"

class RoomManager(
    private val context: Context,
    private val videoClient: VideoClient,
    sharedPreferences: SharedPreferences
) {

    private var statsScheduler: StatsScheduler? = null
    private val roomListener = RoomListener()
    private val roomEventSubject = PublishSubject.create<RoomEvent>()
    @VisibleForTesting(otherwise = PRIVATE)
    internal var localParticipantManager: LocalParticipantManager =
            LocalParticipantManager(context, this, sharedPreferences)
    var room: Room? = null
    val roomEvents: Observable<RoomEvent> = roomEventSubject

    fun disconnect() {
        room?.disconnect()
    }

    suspend fun connect(identity: String, roomName: String) {
        roomEventSubject.onNext(Connecting)
        room = try {
            videoClient.connect(identity, roomName, roomListener)
        } catch (e: AuthServiceException) {
            handleTokenException(e, e.error)
        } catch (e: Exception) {
            handleTokenException(e)
        }
    }

    fun sendRoomEvent(roomEvent: RoomEvent) {
        roomEventSubject.onNext(roomEvent)
    }

    private fun handleTokenException(e: Exception, error: AuthServiceError? = null): Room? {
        Timber.e(e, "Failed to retrieve token")
        roomEventSubject.onNext(RoomEvent.TokenError(serviceError = error))
        return null
    }

    fun onResume() {
        localParticipantManager.onResume()
    }

    fun onPause() {
        localParticipantManager.onPause()
    }

    fun toggleLocalVideo() {
        localParticipantManager.toggleLocalVideo()
    }

    fun toggleLocalAudio() {
        localParticipantManager.toggleLocalAudio()
    }

    fun startScreenCapture(captureResultCode: Int, captureIntent: Intent) {
        localParticipantManager.startScreenCapture(captureResultCode, captureIntent)
    }

    fun stopScreenCapture() {
        localParticipantManager.stopScreenCapture()
    }

    fun switchCamera() = localParticipantManager.switchCamera()

    fun sendStatsUpdate(statsReports: List<StatsReport>) {
        room?.let { room ->
            val roomStats = RoomStats(
                    room.remoteParticipants,
                    localParticipantManager.localVideoTrackNames,
                    statsReports
            )
            sendRoomEvent(StatsUpdate(roomStats))
        }
    }

    fun enableLocalAudio() = localParticipantManager.enableLocalAudio()

    fun disableLocalAudio() = localParticipantManager.disableLocalAudio()

    fun enableLocalVideo() = localParticipantManager.enableLocalVideo()

    fun disableLocalVideo() = localParticipantManager.disableLocalVideo()

    inner class RoomListener : Room.Listener {
        override fun onConnected(room: Room) {
            Timber.i("onConnected -> room sid: %s",
                    room.sid)

            startService(context, room.name)

            setupParticipants(room)

            statsScheduler = StatsScheduler(this@RoomManager, room).apply { start() }
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Timber.i("Disconnected from room -> sid: %s, state: %s",
                    room.sid, room.state)

            stopService(context)

            roomEventSubject.onNext(Disconnected)

            localParticipantManager.localParticipant = null

            statsScheduler?.stop()
            statsScheduler = null
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Timber.e(
                    "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                    room.sid,
                    room.state,
                    twilioException.code,
                    twilioException.message)

            if (twilioException.code == ROOM_MAX_PARTICIPANTS_EXCEEDED_EXCEPTION) {
                roomEventSubject.onNext(MaxParticipantFailure)
            } else {
                roomEventSubject.onNext(ConnectFailure)
            }
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)

            remoteParticipant.setListener(RemoteParticipantListener(this@RoomManager))
            sendRoomEvent(RemoteParticipantConnected(remoteParticipant))
        }

        override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)

            sendRoomEvent(RemoteParticipantDisconnected(remoteParticipant.sid))
        }

        override fun onDominantSpeakerChanged(room: Room, remoteParticipant: RemoteParticipant?) {
            Timber.i("DominantSpeakerChanged -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant?.sid)

            roomEventSubject.onNext(DominantSpeakerChanged(remoteParticipant?.sid))
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
                localParticipantManager.localParticipant = localParticipant
                val participants = mutableListOf<Participant>()
                participants.add(localParticipant)
                localParticipant.setListener(LocalParticipantListener(this@RoomManager))

                room.remoteParticipants.forEach {
                    it.setListener(RemoteParticipantListener(this@RoomManager))
                    participants.add(it)
                }

                roomEventSubject.onNext(Connected(participants, room, room.name))
                localParticipantManager.publishLocalTracks()
            }
        }
    }
}
