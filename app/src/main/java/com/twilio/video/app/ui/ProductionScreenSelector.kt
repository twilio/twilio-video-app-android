package com.twilio.video.app.ui

import androidx.appcompat.app.AppCompatActivity
import com.twilio.video.app.ui.login.LoginActivity

class ProductionScreenSelector : ScreenSelector {

    override val loginScreen: Class<out AppCompatActivity>
        get() = LoginActivity::class.java
}
