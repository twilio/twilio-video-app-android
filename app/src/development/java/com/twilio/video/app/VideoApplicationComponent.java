package com.twilio.video.app;

import com.twilio.video.app.auth.DevelopmentAuthModule;
import com.twilio.video.app.data.DevelopmentDataModule;
import com.twilio.video.app.ui.login.DevelopmentLoginActivityModule;
import com.twilio.video.app.ui.login.LoginActivityModule;
import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.splash.SplashActivityModule;

import dagger.Component;

@ApplicationScope
@Component(modules = {
        ApplicationModule.class,
        DevelopmentTreeModule.class,
        DevelopmentDataModule.class,
        DevelopmentAuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        DevelopmentLoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class
})
public interface VideoApplicationComponent extends VideoApplicationGraph {
}
