package com.twilio.video.app.ui.login;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = CommunityLoginActivitySubcomponent.class)
public abstract class CommunityLoginActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(CommunityLoginActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindYourActivityInjectorFactory(CommunityLoginActivitySubcomponent.Builder builder);
}
