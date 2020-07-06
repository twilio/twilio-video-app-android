package com.twilio.audioswitch.android

import android.os.SystemClock

class SystemClockWrapper {

    fun elapsedRealtime() = SystemClock.elapsedRealtime()
}
