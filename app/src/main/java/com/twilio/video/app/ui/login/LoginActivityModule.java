package com.twilio.video.app.ui.login;

import dagger.android.AndroidInjector;

// @Module(subcomponents = LoginActivitySubcomponent.class)
public abstract class LoginActivityModule {
    //    @Binds
    //    @IntoMap
    //    @ClassKey(LoginActivity.class)
    abstract AndroidInjector.Factory<?> bindYourActivityInjectorFactory(
            LoginActivitySubcomponent.Factory factory);
}
