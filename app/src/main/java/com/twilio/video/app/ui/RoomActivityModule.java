package com.twilio.video.app.ui;


import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = RoomActivitySubcomponent.class)
public abstract class RoomActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(RoomActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindYourActivityInjectorFactory(RoomActivitySubcomponent.Builder builder);
}
