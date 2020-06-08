package com.twilio.video.app.ui

import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.ui.login.LoginActivity

class ProductionScreenSelector : ScreenSelector {

    override val loginScreen: Class<out BaseActivity>
        get() = LoginActivity::class.java
}
