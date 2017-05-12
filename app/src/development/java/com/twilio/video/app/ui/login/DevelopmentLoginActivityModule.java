package com.twilio.video.app.ui.login;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = DevelopmentLoginActivitySubcomponent.class)
public abstract class DevelopmentLoginActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(DevelopmentLoginActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindYourActivityInjectorFactory(DevelopmentLoginActivitySubcomponent.Builder builder);
}
