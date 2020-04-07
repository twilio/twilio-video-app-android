package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class BluetoothControllerTest {

    private val context = mock<Context>()
    private val audioManager = mock<AudioManager>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val bluetoothController = BluetoothController(context, audioManager, bluetoothAdapter)

    @Test
    fun `start should register the correct bluetooth receivers`() {
        TODO("Not yet implemented")
    }
}