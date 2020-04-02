package com.twilio.video.app.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDeviceSelector
import com.twilio.video.app.ui.room.RoomViewEvent.ActivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Connect
import com.twilio.video.app.ui.room.RoomViewEvent.DeactivateAudioDevice
import com.twilio.video.app.ui.room.RoomViewEvent.Disconnect
import com.twilio.video.app.ui.room.RoomViewEvent.SelectAudioDevice
import kotlinx.coroutines.launch

class RoomViewModel(
    private val roomManager: RoomManager,
    private val audioDeviceSelector: AudioDeviceSelector
) : ViewModel() {

    private val mutableAudioViewState = MutableLiveData(AudioViewState())

    // TODO Build single ViewState for UI to consume instead of having multiple event streams
    val roomEvents: LiveData<RoomEvent?> = roomManager.viewEvents
    val audioViewState: LiveData<AudioViewState?> = mutableAudioViewState

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
            Disconnect -> { disconnect() }
        }
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

    private fun updateState(action: (oldState: AudioViewState) -> AudioViewState) {
        withState { currentState ->
            mutableAudioViewState.value = action(currentState)
        }
    }

    private fun <R> withState(action: (currentState: AudioViewState) -> R): R {
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
