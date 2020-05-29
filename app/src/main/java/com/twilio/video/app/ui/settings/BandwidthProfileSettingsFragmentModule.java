package com.twilio.video.app.ui.settings;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = BandwidthProfileSettingsFragmentSubcomponent.class)
public abstract class BandwidthProfileSettingsFragmentModule {
    @Binds
    @IntoMap
    @ClassKey(BandwidthProfileSettingsFragment.class)
    abstract AndroidInjector.Factory<?> bindYourFragmentInjectorFactory(BandwidthProfileSettingsFragmentSubcomponent.Factory factory);
}
