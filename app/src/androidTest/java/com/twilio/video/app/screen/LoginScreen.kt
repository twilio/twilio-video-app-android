package com.twilio.video.app.screen

import android.os.Build
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.EmailCredentials
import com.twilio.video.app.R
import com.twilio.video.app.util.retryEspressoAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

// TODO Move to common module as part of https://issues.corp.twilio.com/browse/AHOYAPPS-197

class DisableAutofillAction : ViewAction {
    override fun getConstraints(): Matcher<View>? {
        return Matchers.any(View::class.java)
    }

    override fun getDescription(): String {
        return "Marking view not important for autofill"
    }

    override fun perform(uiController: UiController?, view: View?) {
        // Required to disable autofill suggestions during tests on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
    }
}

fun loginWithEmail(emailCredentials: EmailCredentials) {
    loginWithEmail(emailCredentials.email, emailCredentials.password)
}

private fun loginWithEmail(email: String, password: String) {
    onView(withId(R.id.email_button)).perform(click())
    onView(withId(R.id.email)).perform(
        DisableAutofillAction(),
        typeText(email))
    onView(withId(R.id.button_next)).perform(click())
    retryEspressoAction { onView(withId(R.id.password)).perform(
        DisableAutofillAction(),
        typeText(password))
    }
    onView(withId(R.id.button_done)).perform(click())
}
