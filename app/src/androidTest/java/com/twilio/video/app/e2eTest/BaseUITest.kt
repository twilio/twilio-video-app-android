package com.twilio.video.app.e2eTest

import androidx.test.rule.GrantPermissionRule
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.screen.logout
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class BaseUITest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setUp() {
        loginWithEmail(retrieveEmailCredentials())
    }

    @After
    fun tearDown() {
        retryEspressoAction { clickSettingsMenuItem() }
        logout()
    }
}