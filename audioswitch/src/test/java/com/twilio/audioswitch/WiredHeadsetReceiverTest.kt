package com.twilio.audioswitch

import android.content.Context
import android.content.Intent
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.fail
import org.junit.Test
import kotlin.NullPointerException

class WiredHeadsetReceiverTest {

    private val context = mock<Context>()
    private val logger = mock<LogWrapper>()
    private val wiredDeviceConnectionListener = mock<WiredDeviceConnectionListener>()
    private val wiredHeadsetReceiver = WiredHeadsetReceiver(
            context,
            logger,
            wiredDeviceConnectionListener)

    @Test
    fun `onReceive should notify listener when a wired headset has been plugged in`() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra("state", STATE_UNPLUGGED))
                    .thenReturn(STATE_PLUGGED)
        }

        wiredHeadsetReceiver.onReceive(context, intent)

        verify(wiredDeviceConnectionListener).onDeviceConnected()
    }

    @Test
    fun `onReceive should not notify listener when a wired headset has been plugged in but the listener is null`() {
        wiredHeadsetReceiver.wiredDeviceConnectionListener = null
        val intent = mock<Intent> {
            whenever(mock.getIntExtra("state", STATE_UNPLUGGED))
                    .thenReturn(STATE_PLUGGED)
        }

        try {
            wiredHeadsetReceiver.onReceive(context, intent)
        } catch (e: NullPointerException) {
            fail("NullPointerException should not have been thrown")
        }
    }

    @Test
    fun `onReceive should notify listener when a wired headset has been unplugged`() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra("state", STATE_UNPLUGGED))
                    .thenReturn(STATE_UNPLUGGED)
        }

        wiredHeadsetReceiver.onReceive(context, intent)

        verify(wiredDeviceConnectionListener).onDeviceDisconnected()
    }

    @Test
    fun `onReceive should not notify listener when a wired headset has been unplugged but the listener is null`() {
        wiredHeadsetReceiver.wiredDeviceConnectionListener = null
        val intent = mock<Intent> {
            whenever(mock.getIntExtra("state", STATE_UNPLUGGED))
                    .thenReturn(STATE_UNPLUGGED)
        }

        try {
            wiredHeadsetReceiver.onReceive(context, intent)
        } catch (e: NullPointerException) {
            fail("NullPointerException should not have been thrown")
        }
    }

    @Test
    fun `start should register the broadcast receiver`() {
        wiredHeadsetReceiver.start()

        verify(context).registerReceiver(eq(wiredHeadsetReceiver), isA())
    }

    @Test
    fun `stop should unregister the broadcast receiver`() {
        wiredHeadsetReceiver.stop()

        verify(context).unregisterReceiver(wiredHeadsetReceiver)
    }
}