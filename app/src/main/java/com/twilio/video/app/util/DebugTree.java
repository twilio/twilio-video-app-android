package com.twilio.video.app.util;

import android.util.Log;

import timber.log.Timber;

public class DebugTree extends Timber.DebugTree {
    private final TreeRanger treeRanger;

    public DebugTree(TreeRanger treeRanger) {
        this.treeRanger = treeRanger;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        // Always log in debug
        super.log(priority, tag, message, throwable);

        // Allow the ranger to act accordingly
        switch (priority) {
            case Log.VERBOSE:
            case Log.DEBUG:
            case Log.INFO:
                treeRanger.inform(message);
                break;
            case Log.WARN:
                treeRanger.caution(message);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                if (throwable == null) {
                    treeRanger.alert(new Exception(message));
                } else {
                    treeRanger.alert(throwable);
                }
                break;
        }
    }
}
