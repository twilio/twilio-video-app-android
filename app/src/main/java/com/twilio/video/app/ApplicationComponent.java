package com.twilio.video.app;


import com.twilio.video.app.auth.AuthModule;
import com.twilio.video.app.data.DataModule;
import com.twilio.video.app.data.api.VideoAppServiceModule;
import com.twilio.video.app.ui.LoginActivityModule;
import com.twilio.video.app.ui.RoomActivityModule;
import com.twilio.video.app.ui.SettingsActivityModule;
import com.twilio.video.app.ui.SplashActivityModule;

import dagger.Component;

@ApplicationScope
@Component(modules = {
        ApplicationModule.class,
        TreeModule.class,
        DataModule.class,
        VideoAppServiceModule.class,
        AuthModule.class,
        SplashActivityModule.class,
        LoginActivityModule.class,
        RoomActivityModule.class,
        SettingsActivityModule.class
})
public interface ApplicationComponent {
    void inject(VideoApplication videoApplication);
}
