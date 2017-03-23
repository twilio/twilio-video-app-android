package com.twilio.video.app.util;

import com.google.firebase.crash.FirebaseCrash;

public class FirebaseTreeRanger implements TreeRanger {
    @Override
    public void inform(String message) {
        // No inform implementation for now. We could potentially use Firebase Analytics
    }

    @Override
    public void caution(String message) {
        FirebaseCrash.log(message);
    }

    @Override
    public void alert(Throwable throwable) {
        FirebaseCrash.report(throwable);
    }
}
