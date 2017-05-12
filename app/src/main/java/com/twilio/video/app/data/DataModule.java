package com.twilio.video.app.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.twilio.video.app.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {
    @Provides
    @ApplicationScope
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
