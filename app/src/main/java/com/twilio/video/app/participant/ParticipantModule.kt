package com.twilio.video.app.participant

import com.twilio.video.app.ApplicationScope
import dagger.Module
import dagger.Provides

@Module
class ParticipantModule {

    @Provides
    @ApplicationScope
    fun providesParticipantManager(
    ): ParticipantManager {
        return ParticipantManager()
    }
}