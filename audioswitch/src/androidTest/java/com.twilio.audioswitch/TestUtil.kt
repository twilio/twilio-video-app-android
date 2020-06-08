package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.FakeBluetoothIntentProcessor
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.bluetooth.PreConnectedDeviceListener
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.audioswitch.selection.AudioFocusRequestWrapper
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

internal fun setupFakeAudioDeviceSelector(context: Context):
        Pair<AudioDeviceSelector, BluetoothHeadsetReceiver> {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val logger = LogWrapper()
    val audioDeviceManager =
            AudioDeviceManager(context,
                    logger,
                    audioManager,
                    BuildWrapper(),
                    AudioFocusRequestWrapper())
    val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    val bluetoothIntentProcessor = FakeBluetoothIntentProcessor()
    val bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger, bluetoothIntentProcessor)
    val bluetoothController = BluetoothAdapter.getDefaultAdapter()?.let { bluetoothAdapter ->
        BluetoothController(context,
                audioDeviceManager,
                bluetoothAdapter,
                PreConnectedDeviceListener(logger, bluetoothAdapter),
                bluetoothHeadsetReceiver)
    } ?: run {
        null
    }
    return Pair(AudioDeviceSelector(logger,
            audioDeviceManager,
            wiredHeadsetReceiver,
            bluetoothController),
            bluetoothHeadsetReceiver)
}

internal fun simulateBluetoothConnection(context: Context, bluetoothHeadsetReceiver: BluetoothHeadsetReceiver) {
    val intent = Intent(BluetoothDevice.ACTION_ACL_CONNECTED)
    bluetoothHeadsetReceiver.onReceive(context, intent)
}
