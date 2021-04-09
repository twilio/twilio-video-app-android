package com.twilio.video.app

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import timber.log.Timber

class TwilioPhoneStateListener(
    private val callStateRingingAction: () -> Unit,
    private val callStateIdleAction: () -> Unit
) : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Timber.d("CALL_STATE_RINGING")
                callStateRingingAction()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                Timber.d("CALL_STATE_IDLE")
                callStateIdleAction()
            }
            else -> {}
        }
    }
}
