package com.twilio.video.app.ui.login;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface DevelopmentLoginActivitySubcomponent
        extends AndroidInjector<DevelopmentLoginActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DevelopmentLoginActivity> {}
}
