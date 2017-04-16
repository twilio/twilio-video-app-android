package com.twilio.video.app.auth;

import com.twilio.video.app.base.BaseActivity;

public interface Authenticator {
    Class<? extends BaseActivity> getLoginActivity();
    boolean loggedIn();
    void logout();
}
