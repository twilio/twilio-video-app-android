package com.twilio.video.app.idlingresource

import com.twilio.video.app.ApplicationScope
import dagger.Module
import dagger.Provides

@Module
class IdlingResourceModule(private val ICountingIdlingResource: ICountingIdlingResource) {

    @Provides
    @ApplicationScope
    fun providesCountingIdlingResource(): ICountingIdlingResource {
        return ICountingIdlingResource
    }

}
