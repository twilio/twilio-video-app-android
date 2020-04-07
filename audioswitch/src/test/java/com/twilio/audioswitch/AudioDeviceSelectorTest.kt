package com.twilio.audioswitch

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class AudioDeviceSelectorTest {

    val context = mock<Context>()
    private val bluetoothController = BluetoothController(context)
    private lateinit var audioDeviceSelector: AudioDeviceSelector

    @Test
    fun `stop should throw an IllegalStateException if current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should throw an IllegalStateException if current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should throw an IllegalStateException if current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `selectDevice should throw an IllegalStateException if current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `start should throw an IllegalStateException if current state is started`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should throw an IllegalStateException if current state is started`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `start should throw an IllegalStateException if current state is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should throw an IllegalStateException if current state is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `selectDevice should throw an IllegalStateException if current state is activated`() {
        TODO("Not yet implemented")
    }
}