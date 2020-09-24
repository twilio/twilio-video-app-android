package com.twilio.video.app.e2eTest

import androidx.test.rule.GrantPermissionRule
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.util.allowAllPermissions
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.uiDevice
import org.junit.Before
import org.junit.Rule

@E2ETest
open class BaseE2ETest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    open fun setUp() {
        loginWithEmail(retrieveEmailCredentials())
        uiDevice().run {
            allowAllPermissions()
        }
    }
}
