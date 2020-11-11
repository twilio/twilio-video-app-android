package com.twilio.video.app.ui.room

import android.Manifest.permission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.Participant
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.buildParticipantViewState
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.ui.room.RoomEvent.ConnectFailure
import com.twilio.video.app.ui.room.RoomEvent.Connected
import com.twilio.video.app.ui.room.RoomEvent.Connecting
import com.twilio.video.app.ui.room.RoomEvent.Disconnected
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioDisabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioEnabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioOff
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.AudioOn
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.ScreenCaptureOff
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.ScreenCaptureOn
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoDisabled
import com.twilio.video.app.ui.room.RoomEvent.LocalParticipantEvent.VideoEnabled
import com.twilio.video.app.ui.room.RoomEvent.MaxParticipantFailure
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.MuteRemoteParticipant
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.NetworkQualityLevelChange
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.RemoteParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.RemoteParticipantDisconnected
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.ScreenTrackUpdated
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.TrackSwitchOff
import com.twilio.video.app.ui.room.RoomEvent.RemoteParticipantEvent.VideoTrackUpdated
import com.twilio.video.app.ui.room.RoomEvent.StatsUpdate
import com.twilio.video.app.ui.room.RoomEvent.TokenError
import com.twilio.video.app.ui.room.RoomViewConfiguration.Lobby
import com.twilio.video.app.ui.room.RoomViewEffect.PermissionsDenied
import com.twilio.video.app.ui.room.RoomViewEffect.ShowConnectFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowMaxParticipantFailureDialog
import com.twilio.video.app.ui.room.RoomViewEffect.ShowTokenErrorDialog
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.DeactivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.DisableLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.DisableLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.EnableLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.EnableLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.OnPause
import com.twilio.video.app.ui.room.RoomViewEvent.OnResume
import com.twilio.video.app.ui.room.RoomViewEvent.PinParticipant
import com.twilio.video.app.ui.room.RoomViewEvent.ScreenTrackRemoved
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.StartScreenCapture
import com.twilio.video.app.ui.room.RoomViewEvent.StopScreenCapture
import com.twilio.video.app.ui.room.RoomViewEvent.SwitchCamera
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalAudio
import com.twilio.video.app.ui.room.RoomViewEvent.ToggleLocalVideo
import com.twilio.video.app.ui.room.RoomViewEvent.VideoTrackRemoved
import com.twilio.video.app.util.PermissionUtil
import com.twilio.video.app.util.plus
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.uniflow.androidx.flow.AndroidDataFlow
import io.uniflow.core.flow.actionOn
import io.uniflow.core.flow.data.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RoomViewModel(
    private val roomManager: RoomManager,
    private val audioSwitch: AudioSwitch,
    private val permissionUtil: PermissionUtil,
    private val participantManager: ParticipantManager = ParticipantManager(),
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val rxDisposables: CompositeDisposable = CompositeDisposable(),
    scheduler: Scheduler = AndroidSchedulers.mainThread(),
    initialViewState: RoomViewState = RoomViewState(participantManager.primaryParticipant)
) : AndroidDataFlow(defaultState = initialViewState) {

    private var permissionCheckRetry = false

    init {
        audioSwitch.start { audioDevices, selectedDevice ->
            actionOn<RoomViewState> { currentState ->
                setState {
                    currentState.copy(
                        selectedDevice = selectedDevice,
                        availableAudioDevices = audioDevices
                    )
                }
            }
        }

        rxDisposables + roomManager.roomEvents
                .observeOn(scheduler)
                .subscribe({
            observeRoomEvents(it)
        }, {
            Timber.e(it, "Error in RoomManager RoomEvent stream")
        })
    }

    override fun onCleared() {
        super.onCleared()
        audioSwitch.stop()
        rxDisposables.clear()
    }

    fun processInput(viewEvent: RoomViewEvent) {
        Timber.d("View Event: $viewEvent")

        when (viewEvent) {
            OnResume -> checkPermissions()
            OnPause -> roomManager.onPause()
            is SelectAudioDevice -> {
                audioSwitch.selectDevice(viewEvent.device)
            }
            ActivateAudioDevice -> { audioSwitch.activate() }
            DeactivateAudioDevice -> { audioSwitch.deactivate() }
            is Connect -> {
                connect(viewEvent.identity, viewEvent.roomName)
            }
            is PinParticipant -> {
                participantManager.changePinnedParticipant(viewEvent.sid)
                updateParticipantViewState()
            }
            ToggleLocalVideo -> roomManager.toggleLocalVideo()
            EnableLocalVideo -> roomManager.enableLocalVideo()
            DisableLocalVideo -> roomManager.disableLocalVideo()
            ToggleLocalAudio -> roomManager.toggleLocalAudio()
            EnableLocalAudio -> roomManager.enableLocalAudio()
            DisableLocalAudio -> roomManager.disableLocalAudio()
            is StartScreenCapture -> roomManager.startScreenCapture(
                    viewEvent.captureResultCode, viewEvent.captureIntent)
            StopScreenCapture -> roomManager.stopScreenCapture()
            SwitchCamera -> roomManager.switchCamera()
            is VideoTrackRemoved -> {
                participantManager.updateParticipantVideoTrack(viewEvent.sid, null)
                updateParticipantViewState()
            }
            is ScreenTrackRemoved -> {
                participantManager.updateParticipantScreenTrack(viewEvent.sid, null)
                updateParticipantViewState()
            }
            Disconnect -> roomManager.disconnect()
        }
    }

    private fun checkPermissions() {
        val isCameraEnabled = permissionUtil.isPermissionGranted(permission.CAMERA)
        val isMicEnabled = permissionUtil.isPermissionGranted(permission.RECORD_AUDIO)

        setState {
            it.copy(isCameraEnabled = isCameraEnabled, isMicEnabled = isMicEnabled)
        }
        if (isCameraEnabled && isMicEnabled) {
            roomManager.onResume()
        } else {
            if (!permissionCheckRetry) {
                action {
                    sendEvent {
                        permissionCheckRetry = true
                        PermissionsDenied
                    }
                }
            }
        }
    }

    private fun observeRoomEvents(roomEvent: RoomEvent) {
        Timber.d("observeRoomEvents: %s", roomEvent)
        when (roomEvent) {
            is Connecting -> {
                showConnectingViewState()
            }
            is Connected -> {
                showConnectedViewState(roomEvent.roomName)
                checkParticipants(roomEvent.participants)
                action { sendEvent { RoomViewEffect.Connected(roomEvent.room) } }
            }
            is Disconnected -> showLobbyViewState()
            is DominantSpeakerChanged -> {
                participantManager.changeDominantSpeaker(roomEvent.newDominantSpeakerSid)
                updateParticipantViewState()
            }
            is ConnectFailure -> action {
                sendEvent {
                    showLobbyViewState()
                    ShowConnectFailureDialog
                }
            }
            is MaxParticipantFailure -> action {
                sendEvent {
                    showLobbyViewState()
                    ShowMaxParticipantFailureDialog
                }
            }
            is TokenError -> action {
                sendEvent {
                    showLobbyViewState()
                    ShowTokenErrorDialog(roomEvent.serviceError)
                }
            }
            is RemoteParticipantEvent -> handleRemoteParticipantEvent(roomEvent)
            is LocalParticipantEvent -> handleLocalParticipantEvent(roomEvent)
            is StatsUpdate -> setState { it.copy(roomStats = roomEvent.roomStats) }
        }
    }

    private fun handleRemoteParticipantEvent(remoteParticipantEvent: RemoteParticipantEvent) {
        when (remoteParticipantEvent) {
            is RemoteParticipantConnected -> addParticipant(remoteParticipantEvent.participant)
            is VideoTrackUpdated -> {
                participantManager.updateParticipantVideoTrack(remoteParticipantEvent.sid,
                        remoteParticipantEvent.videoTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
            }
            is TrackSwitchOff -> {
                participantManager.updateParticipantVideoTrack(remoteParticipantEvent.sid,
                        VideoTrackViewState(remoteParticipantEvent.videoTrack,
                                remoteParticipantEvent.switchOff))
                updateParticipantViewState()
            }
            is ScreenTrackUpdated -> {
                participantManager.updateParticipantScreenTrack(remoteParticipantEvent.sid,
                        remoteParticipantEvent.screenTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
            }
            is MuteRemoteParticipant -> {
                participantManager.muteParticipant(remoteParticipantEvent.sid,
                        remoteParticipantEvent.mute)
                updateParticipantViewState()
            }
            is NetworkQualityLevelChange -> {
                participantManager.updateNetworkQuality(remoteParticipantEvent.sid,
                        remoteParticipantEvent.networkQualityLevel)
                updateParticipantViewState()
            }
            is RemoteParticipantDisconnected -> {
                participantManager.removeParticipant(remoteParticipantEvent.sid)
                updateParticipantViewState()
            }
        }
    }

    private fun handleLocalParticipantEvent(localParticipantEvent: LocalParticipantEvent) {
        when (localParticipantEvent) {
            is LocalParticipantEvent.VideoTrackUpdated -> {
                participantManager.updateLocalParticipantVideoTrack(
                        localParticipantEvent.videoTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
                setState { it.copy(isVideoOff = localParticipantEvent.videoTrack == null) }
            }
            AudioOn -> setState { it.copy(isAudioMuted = false) }
            AudioOff -> setState { it.copy(isAudioMuted = true) }
            AudioEnabled -> setState { it.copy(isAudioEnabled = true) }
            AudioDisabled -> setState { it.copy(isAudioEnabled = false) }
            ScreenCaptureOn -> setState { it.copy(isScreenCaptureOn = true) }
            ScreenCaptureOff -> setState { it.copy(isScreenCaptureOn = false) }
            VideoEnabled -> setState { it.copy(isVideoEnabled = true) }
            VideoDisabled -> setState { it.copy(isVideoEnabled = false) }
        }
    }

    private fun addParticipant(participant: Participant) {
        val participantViewState = buildParticipantViewState(participant)
        participantManager.addParticipant(participantViewState)
        updateParticipantViewState()
    }

    private fun showLobbyViewState() {
        action { sendEvent { RoomViewEffect.Disconnected } }
        setState {
            it.copy(configuration = Lobby)
        }
        participantManager.clearRemoteParticipants()
        updateParticipantViewState()
    }

    private fun showConnectingViewState() {
        setState {
            it.copy(configuration = RoomViewConfiguration.Connecting)
        }
    }

    private fun showConnectedViewState(roomName: String) {
        setState {
            it.copy(configuration = RoomViewConfiguration.Connected, title = roomName)
        }
    }

    private fun checkParticipants(participants: List<Participant>) {
        for ((index, participant) in participants.withIndex()) {
            if (index == 0) { // local participant
                participantManager.updateLocalParticipantSid(participant.sid)
            } else {
                participantManager.addParticipant(buildParticipantViewState(participant))
            }
        }
        updateParticipantViewState()
    }

    private fun updateParticipantViewState() {
        setState {
            it.copy(
                    participantThumbnails = participantManager.participantThumbnails,
                    primaryParticipant = participantManager.primaryParticipant
            )
        }
    }

    private fun connect(identity: String, roomName: String) =
        backgroundScope.launch {
            roomManager.connect(
                    identity,
                    roomName)
        }

    private fun setState(action: (currentState: RoomViewState) -> UIState) =
        actionOn<RoomViewState> { currentState -> setState { action(currentState) } }

    class RoomViewModelFactory(
        private val roomManager: RoomManager,
        private val audioDeviceSelector: AudioSwitch,
        private val permissionUtil: PermissionUtil
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomViewModel(roomManager, audioDeviceSelector, permissionUtil) as T
        }
    }
}
