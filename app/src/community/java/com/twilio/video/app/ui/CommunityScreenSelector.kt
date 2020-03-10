package com.twilio.video.app.ui

import androidx.appcompat.app.AppCompatActivity
import com.twilio.video.app.ui.login.CommunityLoginActivity

class CommunityScreenSelector : ScreenSelector {

    override val loginScreen: Class<out AppCompatActivity>
        get() = CommunityLoginActivity::class.java
}