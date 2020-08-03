package com.twilio.video.app.ui.room

import androidx.appcompat.app.AppCompatActivity
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceSelector

fun AppCompatActivity.test(audioDeviceSelector: AudioDeviceSelector) {
    audioDeviceSelector.start { list, selectedDevice ->
        println("when list of devices change selectedAudioDevice = ${selectedDevice?.name} audioDevices = $list")
    }
    audioDeviceSelector.availableAudioDevices.find { it is AudioDevice.BluetoothHeadset }?.let {
        println("Trying to select BT headset $it")
        audioDeviceSelector.selectDevice(it)
    }
    audioDeviceSelector.activate()
}