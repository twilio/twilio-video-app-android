package com.twilio.audioswitch.android

import android.os.Build

class BuildWrapper {

    fun getVersion(): Int = Build.VERSION.SDK_INT
}