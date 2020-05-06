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
import com.twilio.video.app.udf.BaseViewModel
import com.twilio.video.app.ui.room.RoomEvent.Connected
import com.twilio.video.app.ui.room.RoomEvent.Disconnected
import com.twilio.video.app.ui.room.RoomEvent.DominantSpeakerChanged
import com.twilio.video.app.ui.room.RoomEvent.NewRemoteVideoTrack
import com.twilio.video.app.ui.room.RoomEvent.ParticipantConnected
import com.twilio.video.app.ui.room.RoomEvent.ParticipantDisconnected
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.DeactivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.LocalVideoTrackPublished
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import kotlinx.coroutines.launch
import timber.log.Timber

class RoomViewModel(
    private val roomManager: RoomManager,
    private val audioDeviceSelector: AudioDeviceSelector,
    private val participantManager: ParticipantManager = ParticipantManager()
) : BaseViewModel<RoomViewEvent, RoomViewState, RoomViewEffect>() {

    // TODO Use another type of observable here like a Coroutine flow
    val roomEvents: LiveData<RoomEvent?> = Transformations.map(roomManager.viewEvents, ::observeRoomEvents)
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

    private fun observeRoomEvents(roomEvent: RoomEvent?): RoomEvent? {
        when (roomEvent) {
            is Connected -> {
                updateState { it.copy(room = roomEvent.room, roomName = roomEvent.roomName) }
                checkRemoteParticipants(roomEvent.remoteParticipants)
            }
            is Disconnected -> {
                participantManager.clearParticipants()
                updateState { it.copy(participantThumbnails = null) }
            }
            is NewRemoteVideoTrack -> addParticipantView(roomEvent.participant)
            is ParticipantConnected -> addParticipantView(roomEvent.participant)
            is DominantSpeakerChanged -> addParticipantView(roomEvent.participant)
            is ParticipantDisconnected -> {
                participantManager.removeParticipant(roomEvent.participant)
                updateParticipantViewState()
            }
        }
        updateState { it.copy(roomEvent = roomEvent) }
        return roomEvent
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
