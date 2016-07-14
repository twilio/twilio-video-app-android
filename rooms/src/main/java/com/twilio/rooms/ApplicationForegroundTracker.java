package com.twilio.rooms;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

final class ApplicationForegroundTracker implements Application.ActivityLifecycleCallbacks {
    private Activity currentActivity;
    private long nativeCore;

    public ApplicationForegroundTracker(long nativeCore) {
        this.nativeCore = nativeCore;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        nativeOnApplicationForeground(nativeCore);
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (currentActivity == null ||
                currentActivity == activity) {
            nativeOnApplicationBackground(nativeCore);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    private native void nativeOnApplicationForeground(long nativeCore);
    private native void nativeOnApplicationBackground(long nativeCore);
}
