package com.twilio.video.app.idlingresource

interface ICountingIdlingResource {

    fun increment()

    fun decrement()
}