package com.twilio.video.app.ui.settings;

import dagger.android.AndroidInjector;

// @Module(subcomponents = SettingsFragmentSubcomponent.class)
public abstract class SettingsFragmentModule {
    //    @Binds
    //    @IntoMap
    //    @ClassKey(SettingsFragment.class)
    abstract AndroidInjector.Factory<?> bindYourFragmentInjectorFactory(
            SettingsFragmentSubcomponent.Factory factory);
}
