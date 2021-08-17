package com.twilio.video.app.ui.room

import android.app.Application
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.app.participant.ParticipantManager
import com.twilio.video.app.util.PermissionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class RoomViewModelModule {

    @Provides
    fun providesPermissionUtil(application: Application) = PermissionUtil(application)

    @Provides
    fun providesParticipantManager() = ParticipantManager()

    @Provides
    fun providesInitialViewState(participantManager: ParticipantManager) = RoomViewState(participantManager.primaryParticipant)

    @Provides
    fun providesAudioSwitch(application: Application): AudioSwitch =
        AudioSwitch(application,
            loggingEnabled = true,
            preferredDeviceList = listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Speakerphone::class.java,
                AudioDevice.Earpiece::class.java))
}
