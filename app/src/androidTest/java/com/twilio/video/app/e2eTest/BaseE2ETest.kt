package com.twilio.video.app.e2eTest

import android.app.Activity
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.util.allowAllPermissions
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.uiDevice
import java.util.concurrent.TimeoutException
import org.junit.Before

@E2ETest
open class BaseE2ETest {
    @Before
    open fun setUp() {
        // wait for google/firebase auth activity to overlay
        waitUntilActivityVisible<com.firebase.ui.auth.ui.idp.AuthMethodPickerActivity>(5000)
        // start test
        loginWithEmail(retrieveEmailCredentials())
        uiDevice().run {
            try {
                allowAllPermissions()
            } catch (e: TimeoutException) {
                Log.w("VideoApiUtils", "Permissions dialog not detected")
                // try running the test anyway
            }
        }
    }

    open fun getActivityInstance(): Activity? {
        var currentActivity: Activity ? = null
        getInstrumentation().runOnMainSync {
            val resumedActivities: Collection<*> =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED)
            if (resumedActivities.iterator().hasNext()) {
                currentActivity = resumedActivities.iterator().next() as Activity
            }
        }
        return currentActivity
    }

    inline fun <reified T : Activity> isVisible(): Boolean {
        return T::class.java.name == getActivityInstance()!!::class.java.name
    }

    inline fun <reified T : Activity> waitUntilActivityVisible(timeout: Long) {
        val startTime = System.currentTimeMillis()
        while (!isVisible<T>()) {
            Thread.sleep(100)
            if (System.currentTimeMillis() - startTime >= timeout) {
                throw AssertionError(
                    "Activity ${T::class.java.simpleName} not visible after $timeout milliseconds")
            }
        }
    }
}
