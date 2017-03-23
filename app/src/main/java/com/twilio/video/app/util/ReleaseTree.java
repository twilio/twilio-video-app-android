package com.twilio.video.app.util;

import android.util.Log;

import timber.log.Timber;

public class ReleaseTree extends Timber.Tree {
    private final TreeRanger treeRanger;

    public ReleaseTree(TreeRanger treeRanger) {
        this.treeRanger = treeRanger;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
         // No logging in release, but we allow the ranger to still act
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
