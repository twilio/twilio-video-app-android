package com.twilio.conversations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twilio.conversations.TwilioConversationsClient;


public final class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TwilioConversationsClient.isInitialized()) {
            nativeOnApplicationWakeUp();
        }
    }

    private native void nativeOnApplicationWakeUp();
}
