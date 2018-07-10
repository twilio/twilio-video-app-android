package com.twilio.video.app;

import com.twilio.video.app.auth.CommunityAuthModule;
import com.twilio.video.app.data.CommunityDataModule;
import com.twilio.video.app.ui.login.CommunityLoginActivityModule;
import com.twilio.video.app.ui.login.LoginActivityModule;
import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.splash.SplashActivityModule;
import dagger.Component;

@ApplicationScope
@Component(
    modules = {
        ApplicationModule.class,
        CommunityTreeModule.class,
        CommunityDataModule.class,
        CommunityAuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        CommunityLoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class
    }
)
public interface VideoApplicationComponent extends VideoApplicationGraph {}
