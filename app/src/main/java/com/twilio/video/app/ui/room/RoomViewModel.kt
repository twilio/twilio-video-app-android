package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.video.Room
import com.twilio.video.Room.State.CONNECTED
import com.twilio.video.Room.State.DISCONNECTED
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.participant.buildParticipantViewState
import com.twilio.video.app.ui.room.RoomEvent.NewRemoteVideoTrack
import com.twilio.video.app.ui.room.RoomEvent.RoomState
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

    private val mutableAudioViewState = MutableLiveData(RoomViewState())

    // TODO Build single ViewState for UI to consume instead of having multiple event streams
    val roomEvents: LiveData<RoomEvent?> = Transformations.map(roomManager.viewEvents, ::observeRoomEvents)
    val roomViewState: LiveData<RoomViewState?> = mutableAudioViewState

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
                selectAudioDevice(viewEvent.device)
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
                addLocalParticipantView(viewEvent.participantViewState)
            }
            Disconnect -> { disconnect() }
        }
    }

    private fun observeRoomEvents(roomEvent: RoomEvent?): RoomEvent? {
        when (roomEvent) {
            is RoomState -> {
                roomEvent.room?.let { room ->
                    when (room.state) {
                        CONNECTED -> {
                            checkRemoteParticipants(room)
                        }
                        DISCONNECTED -> {
                            participantManager.clearParticipants()
                            updateState { it.copy(participantThumbnails = null) }
                        }
                        else -> {}
                    }
                }
            }
            is NewRemoteVideoTrack -> {
                participantManager.updateParticipants(roomEvent.participantViewState)
                updateState { it.copy(participantThumbnails = participantManager.participants) }
            }
        }
        return roomEvent
    }

    private fun checkRemoteParticipants(room: Room) {
        room.remoteParticipants.let { participants ->
            participants.forEach { participantManager.updateParticipants(buildParticipantViewState(it)) }
            updateState { it.copy(participantThumbnails = participantManager.participants) }
        }
    }

    private fun addLocalParticipantView(participantViewState: ParticipantViewState) {
        participantManager.updateParticipants(participantViewState)
        updateState { it.copy(participantThumbnails = participantManager.participants) }
    }

    private fun selectAudioDevice(device: AudioDevice) {
        audioDeviceSelector.selectDevice(device)
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

    private fun disconnect() {
        roomManager.disconnect()
    }

    private fun updateState(action: (oldState: RoomViewState) -> RoomViewState) {
        withState { currentState ->
            mutableAudioViewState.value = action(currentState)
        }
    }

    private fun <R> withState(action: (currentState: RoomViewState) -> R): R {
        val oldState = mutableAudioViewState.value
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
