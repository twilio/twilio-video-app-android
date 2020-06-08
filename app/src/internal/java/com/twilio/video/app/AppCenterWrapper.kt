package com.twilio.video.app

import android.app.Application
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.distribute.Distribute

fun startAppcenter(application: Application) {
    AppCenter.start(application, BuildConfig.APPCENTER_APP_KEY, Distribute::class.java)
}
