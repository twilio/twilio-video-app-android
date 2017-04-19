package com.twilio.video.app.ui.splash;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = SplashActivitySubcomponent.class)
public abstract class SplashActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(SplashActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindYourActivityInjectorFactory(SplashActivitySubcomponent.Builder builder);
}
