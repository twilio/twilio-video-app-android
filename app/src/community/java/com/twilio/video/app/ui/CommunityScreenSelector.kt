package com.twilio.video.app.ui

import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.ui.login.CommunityLoginActivity

class CommunityScreenSelector : ScreenSelector {

    override val loginScreen: Class<out BaseActivity>
        get() = CommunityLoginActivity::class.java
}
