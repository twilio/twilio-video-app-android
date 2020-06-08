package com.twilio.audioswitch.wired

import android.content.Context
import android.content.Intent
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.LogWrapper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test

class WiredHeadsetReceiverTest {

    private val context = mock<Context>()
    private val logger = mock<LogWrapper>()
    private val wiredDeviceConnectionListener = mock<WiredDeviceConnectionListener>()
    private val wiredHeadsetReceiver = WiredHeadsetReceiver(
            context,
            logger)

    @Test
    fun `onReceive should notify listener when a wired headset has been plugged in`() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra("state", STATE_UNPLUGGED))
                    .thenReturn(STATE_PLUGGED)
        }
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        wiredHeadsetReceiver.onReceive(context, intent)

        verify(wiredDeviceConnectionListener).onDeviceConnected()
    }

    @Test
    fun `onReceive should not notify listener when a wired headset has been plugged in but the listener is null`() {
        wiredHeadsetReceiver.deviceListener = null
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
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        wiredHeadsetReceiver.onReceive(context, intent)

        verify(wiredDeviceConnectionListener).onDeviceDisconnected()
    }

    @Test
    fun `onReceive should not notify listener when a wired headset has been unplugged but the listener is null`() {
        wiredHeadsetReceiver.deviceListener = null
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
    fun `start should register the device listener`() {
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        assertThat(wiredHeadsetReceiver.deviceListener, equalTo(wiredDeviceConnectionListener))
    }

    @Test
    fun `start should register the broadcast receiver`() {
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        verify(context).registerReceiver(eq(wiredHeadsetReceiver), isA())
    }

    @Test
    fun `stop should close resources successfully`() {
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        wiredHeadsetReceiver.stop()

        assertThat(wiredHeadsetReceiver.deviceListener, `is`(nullValue()))
        verify(context).unregisterReceiver(wiredHeadsetReceiver)
    }
}
