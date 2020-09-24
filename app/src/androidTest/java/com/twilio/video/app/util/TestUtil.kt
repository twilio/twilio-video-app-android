package com.twilio.video.app.util

import android.content.Context
import androidx.annotation.IdRes
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.twilio.video.app.EmailCredentials
import com.twilio.video.app.TestCredentials
import java.io.InputStreamReader
import java.util.UUID
import junit.framework.AssertionFailedError

fun retryEspressoAction(timeoutInMillis: Long = 10000L, espressoAction: () -> Unit) {
    val startTime = System.currentTimeMillis()
    var currentTime = 0L
    var exception: Throwable? = null
    while (currentTime <= timeoutInMillis) {
        currentTime = try {
            espressoAction()
            return
        } catch (e: Exception) {
            exception = e
            countDown(startTime)
        } catch (e: AssertionFailedError) {
            exception = e
            countDown(startTime)
        }
    }
    throw AssertionError("Timeout occurred while attempting to find a matching view", exception)
}

fun getTargetContext(): Context = getInstrumentation().targetContext

fun getString(@IdRes stringId: Int) = getTargetContext().getString(stringId)

fun getStringArray(@IdRes stringArrayId: Int) = getTargetContext().resources.getStringArray(stringArrayId)

fun retrieveEmailCredentials(): EmailCredentials {
    val reader = InputStreamReader(getInstrumentation().context.assets.open("Credentials/TestCredentials.json"))
    val jsonReader = JsonReader(reader)
    return (Gson().fromJson(jsonReader, TestCredentials::class.java) as TestCredentials).email_sign_in_user
}

fun randomUUID() = UUID.randomUUID().toString()

private fun countDown(startTime: Long): Long {
    Thread.sleep(10)
    return System.currentTimeMillis() - startTime
}
