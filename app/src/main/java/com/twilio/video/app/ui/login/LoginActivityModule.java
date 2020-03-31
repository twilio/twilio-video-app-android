package com.twilio.video.app.ui.login;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = LoginActivitySubcomponent.class)
public abstract class LoginActivityModule {
    @Binds
    @IntoMap
    @ClassKey(LoginActivity.class)
    abstract AndroidInjector.Factory<?> bindYourActivityInjectorFactory(
            LoginActivitySubcomponent.Factory factory);
}
