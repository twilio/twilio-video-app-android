package com.twilio.video.app.ui.splash;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface SplashActivitySubcomponent extends AndroidInjector<SplashActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SplashActivity> {}
}
