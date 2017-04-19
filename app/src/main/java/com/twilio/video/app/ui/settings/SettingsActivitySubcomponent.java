package com.twilio.video.app.ui.settings;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface SettingsActivitySubcomponent extends AndroidInjector<SettingsActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SettingsActivity> {}
}
