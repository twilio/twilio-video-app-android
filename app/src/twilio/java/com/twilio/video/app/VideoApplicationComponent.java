package com.twilio.video.app;

import com.twilio.video.app.auth.AuthModule;
import com.twilio.video.app.data.ProductionDataModule;
import com.twilio.video.app.data.api.TwilioVideoAppServiceModule;
import com.twilio.video.app.ui.login.LoginActivityModule;
import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.splash.SplashActivityModule;

import dagger.Component;

@ApplicationScope
@Component(modules = {
        ApplicationModule.class,
        TreeModule.class,
        ProductionDataModule.class,
        TwilioVideoAppServiceModule.class,
        AuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class
})
public interface VideoApplicationComponent extends VideoApplicationGraph {
}

