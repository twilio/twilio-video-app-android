package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.ui.room.RoomState.Connected
import com.twilio.video.app.ui.room.RoomState.Disconnected
import com.twilio.video.app.ui.room.RoomState.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomState.NewRemoteVideoTrack
import com.twilio.video.app.ui.room.RoomState.ParticipantConnected
import com.twilio.video.app.ui.room.RoomState.ParticipantDisconnected
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.DeactivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.LocalVideoTrackPublished
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import kotlinx.coroutines.launch

class RoomViewModel(
    private val roomManager: RoomManager,
    private val audioDeviceSelector: AudioDeviceSelector,
    private val participantManager: ParticipantManager = ParticipantManager()
) : ViewModel() {

    // TODO Use another type of observable here like a Coroutine flow
    val roomEvents: LiveData<RoomState?> = Transformations.map(roomManager.viewEvents, ::observeRoomEvents)
    private val mutableRoomViewState = MutableLiveData(RoomViewState())
    val roomViewState: LiveData<RoomViewState?> = mutableRoomViewState

    init {
        audioDeviceSelector.start { audioDevices, selectedDevice ->
            updateState { it.copy(
                selectedDevice = selectedDevice,
                availableAudioDevices = audioDevices)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioDeviceSelector.stop()
    }

    fun processInput(viewEvent: RoomViewEvent) {
        when (viewEvent) {
            is SelectAudioDevice -> {
                audioDeviceSelector.selectDevice(viewEvent.device)
            }
            ActivateAudioDevice -> { audioDeviceSelector.activate() }
            DeactivateAudioDevice -> { audioDeviceSelector.deactivate() }
            is Connect -> {
                connect(
                        viewEvent.identity,
                        viewEvent.roomName,
                        viewEvent.isNetworkQualityEnabled)
            }
            is LocalVideoTrackPublished -> {
                addParticipantView(viewEvent.participantViewState)
            }
            Disconnect -> roomManager.disconnect()
        }
    }

    private fun observeRoomEvents(roomState: RoomState?): RoomState? {
        when (roomState) {
            is Connected -> {
                updateState { it.copy(roomName = roomState.roomName) }
                checkRemoteParticipants(roomState.remoteParticipants)
            }
            is Disconnected -> {
                participantManager.clearParticipants()
                updateState { it.copy(participantThumbnails = null) }
            }
            is NewRemoteVideoTrack -> addParticipantView(roomState.participant)
            is ParticipantConnected -> addParticipantView(roomState.participant)
            is DominantSpeakerChanged -> addParticipantView(roomState.participant)
            is ParticipantDisconnected -> {
                participantManager.removeParticipant(roomState.participant)
                updateParticipantViewState()
            }
        }
        updateState { it.copy(roomState = roomState) }
        return roomState
    }

    private fun checkRemoteParticipants(remoteParticipants: List<ParticipantViewState>) {
            remoteParticipants.forEach { participantManager.updateParticipant(it) }
            updateParticipantViewState()
    }

    private fun addParticipantView(participantViewState: ParticipantViewState) {
        participantManager.updateParticipant(participantViewState)
        updateParticipantViewState()
    }

    private fun updateParticipantViewState() {
        updateState { it.copy(participantThumbnails = participantManager.participantThumbnails) }
        updateState { it.copy(primaryParticipant = participantManager.primaryParticipant) }
    }

    private fun connect(
        identity: String,
        roomName: String,
        isNetworkQualityEnabled: Boolean
    ) =
        viewModelScope.launch {
            roomManager.connectToRoom(
                    identity,
                    roomName,
                    isNetworkQualityEnabled)
        }

    private fun updateState(action: (oldState: RoomViewState) -> RoomViewState) {
        withState { currentState ->
            mutableRoomViewState.value = action(currentState)
        }
    }

    private fun <R> withState(action: (currentState: RoomViewState) -> R): R {
        val oldState = mutableRoomViewState.value
        oldState?.let {
            return action(oldState)
        } ?: throw IllegalStateException("ViewState can never be null")
    }

    class RoomViewModelFactory(
        private val roomManager: RoomManager,
        private val audioDeviceSelector: AudioDeviceSelector
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomViewModel(roomManager, audioDeviceSelector) as T
        }
    }
}
