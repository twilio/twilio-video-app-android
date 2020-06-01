package com.twilio.video.app.ui

import com.twilio.video.app.base.BaseActivity

interface ScreenSelector {

    val loginScreen: Class<out BaseActivity>
}
