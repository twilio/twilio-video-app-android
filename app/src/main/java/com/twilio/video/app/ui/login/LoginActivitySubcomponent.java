package com.twilio.video.app.ui.login;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface LoginActivitySubcomponent extends AndroidInjector<LoginActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<LoginActivity> {}
}
