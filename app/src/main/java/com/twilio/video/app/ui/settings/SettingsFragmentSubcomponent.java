package com.twilio.video.app.ui.settings;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface SettingsFragmentSubcomponent extends AndroidInjector<SettingsFragment> {
    @Subcomponent.Factory
    interface Factory extends AndroidInjector.Factory<SettingsFragment> {}
}
