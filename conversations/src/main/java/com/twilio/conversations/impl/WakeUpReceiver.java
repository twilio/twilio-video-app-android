package com.twilio.conversations.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twilio.conversations.TwilioConversationsClient;


public class WakeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TwilioConversationsClient.isInitialized()) {
            nativeOnApplicationWakeUp();
        }
    }

    private native void nativeOnApplicationWakeUp();
}
