package com.twilio.video.app.ui.login;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface CommunityLoginActivitySubcomponent
        extends AndroidInjector<CommunityLoginActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<CommunityLoginActivity> {}
}
