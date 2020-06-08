package com.twilio.video.app.ui.settings;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = SettingsFragmentSubcomponent.class)
public abstract class SettingsFragmentModule {
    @Binds
    @IntoMap
    @ClassKey(SettingsFragment.class)
    abstract AndroidInjector.Factory<?> bindYourFragmentInjectorFactory(
            SettingsFragmentSubcomponent.Factory factory);
}
