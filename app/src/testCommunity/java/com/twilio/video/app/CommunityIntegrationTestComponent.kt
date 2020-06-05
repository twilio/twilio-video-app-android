package com.twilio.video.app

import com.twilio.video.app.auth.CommunityAuthModule
import com.twilio.video.app.data.DataModule
import com.twilio.video.app.ui.login.CommunityLoginActivity
import com.twilio.video.app.ui.login.CommunityLoginActivityModule
import com.twilio.video.app.ui.login.LoginActivityModule
import dagger.Component
import dagger.android.AndroidInjectionModule

@ApplicationScope
@Component(modules = [
    AndroidInjectionModule::class,
    ApplicationModule::class,
    DataModule::class,
    CommunityAuthModule::class,
    LoginActivityModule::class,
    CommunityLoginActivityModule::class
])
interface CommunityIntegrationTestComponent {
    fun inject(testApp: TestApp)
    fun inject(loginActivity: CommunityLoginActivity)
}
