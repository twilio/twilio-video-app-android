package com.twilio.video.app.ui.settings

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
public interface BandwidthProfileSettingsFragmentSubcomponent : AndroidInjector<BandwidthProfileSettingsFragment> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<BandwidthProfileSettingsFragment>
}
