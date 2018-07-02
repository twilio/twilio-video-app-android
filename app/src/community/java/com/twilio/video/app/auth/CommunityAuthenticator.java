package com.twilio.video.app.auth;

import android.content.SharedPreferences;

import com.google.common.base.Strings;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.login.CommunityLoginActivity;

public class CommunityAuthenticator implements Authenticator {
    private final SharedPreferences preferences;

    public CommunityAuthenticator(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public Class<? extends BaseActivity> getLoginActivity() {
        return CommunityLoginActivity.class;
    }

    @Override
    public boolean loggedIn() {
        return !Strings.isNullOrEmpty(preferences.getString(Preferences.DISPLAY_NAME, null));
    }

    @Override
    public void logout() {
        preferences.edit()
                .remove(Preferences.DISPLAY_NAME)
                .apply();
    }
}
