package com.twilio.video.app

import android.content.Context
import androidx.annotation.IdRes
import androidx.test.espresso.NoMatchingViewException
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import junit.framework.AssertionFailedError
import java.io.InputStreamReader
import java.util.concurrent.TimeoutException

fun retryEspressoAction(timeoutInSeconds: Long = 60000L, espressoAction: () -> Unit) {
    val startTime = System.currentTimeMillis()
    var currentTime = 0L
    while (currentTime <= timeoutInSeconds) {
        try {
            espressoAction()
            return
        } catch (e: NoMatchingViewException) {
            currentTime = countDown(startTime)
        } catch (e: AssertionFailedError) {
            currentTime = countDown(startTime)
        }
    }
    throw TimeoutException("Timeout occurred while attempting to find a matching view")
}

fun getTargetContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

fun getString(@IdRes stringId: Int) = getTargetContext().getString(stringId)

fun retrieveEmailCredentials(): EmailCredentials {
    val reader = InputStreamReader(InstrumentationRegistry.getInstrumentation().context.assets.open("Credentials/TestCredentials.json"))
    val jsonReader = JsonReader(reader)
    return (Gson().fromJson(jsonReader, TestCredentials::class.java) as TestCredentials).email_sign_in_user
}

private fun countDown(startTime: Long): Long {
    Thread.sleep(10)
    return System.currentTimeMillis() - startTime
}
