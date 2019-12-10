package com.twilio.video.app

import com.twilio.video.app.data.DataModule
import com.twilio.video.app.ui.login.LoginActivity
import com.twilio.video.app.ui.login.LoginActivityModule

import dagger.Component

@ApplicationScope
@Component(modules = [
    ApplicationModule::class,
    DataModule::class,
    TestWrapperAuthModule::class,
    TestAuthModule::class,
    LoginActivityModule::class
])
interface IntegrationTestComponent {
    fun inject(testApp: TestApp)
    fun inject(loginActivity: LoginActivity)
}
