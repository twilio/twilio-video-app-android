package com.twilio.video.app.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.ui.login.LoginActivity;

public class FirebaseAuthenticator implements Authenticator {
    @Override
    public Class<? extends BaseActivity> getLoginActivity() {
        return LoginActivity.class;
    }

    @Override
    public boolean loggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}
