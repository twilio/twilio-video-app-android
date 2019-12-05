package com.twilio.video.app

import androidx.test.espresso.idling.CountingIdlingResource
import com.twilio.video.app.idlingresource.ICountingIdlingResource

class CountingIdlingResourceWrapper(private val resource: CountingIdlingResource) : ICountingIdlingResource {

    override fun increment() {
        resource.increment()
    }

    override fun decrement() {
        resource.decrement()
    }

}