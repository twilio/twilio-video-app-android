package com.twilio.rooms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twilio.common.internal.Logger;


public final class WakeUpReceiver extends BroadcastReceiver {
    public static final String NATIVE_CORE_PTR = "NATIVE_CORE_PTR";

    @Override
    public void onReceive(Context context, Intent intent) {
        long nativeCore = intent.getLongExtra(NATIVE_CORE_PTR, 0);
        if(nativeCore == 0) {
            Logger.getLogger(this.getClass()).e("WakeUpReceiver called without a valid native core pointer");
            return;
        }

        if (TwilioConversationsClient.isInitialized()) {
            nativeOnApplicationWakeUp(nativeCore);
        }
    }

    private native void nativeOnApplicationWakeUp(long nativeCore);
}
