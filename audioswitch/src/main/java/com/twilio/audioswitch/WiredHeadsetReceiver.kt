package com.twilio.audioswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private const val TAG = "WiredHeadsetReceiver"
internal const val STATE_UNPLUGGED = 0
internal const val STATE_PLUGGED = 1

internal class WiredHeadsetReceiver(
    private val context: Context,
    private val logger: LogWrapper,
    var wiredDeviceConnectionListener: WiredDeviceConnectionListener? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.getIntExtra("state", STATE_UNPLUGGED).let { state ->
            if (state == STATE_PLUGGED) {
                intent.getStringExtra("name").let { name ->
                    logger.d(TAG, "Wired headset device ${name ?: ""} connected")
                }
                wiredDeviceConnectionListener?.onDeviceConnected()
            } else {
                intent.getStringExtra("name").let { name ->
                    logger.d(TAG, "Wired headset device ${name ?: ""} disconnected")
                }
                wiredDeviceConnectionListener?.onDeviceDisconnected()
            }
        }
    }

    fun start() {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_HEADSET_PLUG))
    }

    fun stop() {
        context.unregisterReceiver(this)
    }
}