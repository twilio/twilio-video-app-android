package com.twilio.video.app;

import com.twilio.video.app.auth.AuthModule;
import com.twilio.video.app.data.DataModule;
import com.twilio.video.app.data.api.VideoAppServiceModule;
import com.twilio.video.app.ui.ScreenSelectorModule;
import com.twilio.video.app.ui.login.LoginActivityModule;
import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.room.RoomManagerModule;
import com.twilio.video.app.ui.room.VideoServiceModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.settings.SettingsFragmentModule;
import com.twilio.video.app.ui.splash.SplashActivityModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@ApplicationScope
@Component(
    modules = {
        AndroidInjectionModule.class,
        ApplicationModule.class,
        TreeModule.class,
        DataModule.class,
        VideoAppServiceModule.class,
        ScreenSelectorModule.class,
        AuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class,
        SettingsFragmentModule.class,
        VideoServiceModule.class,
        RoomManagerModule.class,
        AudioRouterModule.class
    }
)
public interface VideoApplicationComponent {
    void inject(VideoApplication application);
}
