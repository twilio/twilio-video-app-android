package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.conversations.TwilioConversationsClient;


public class TwilioConversationsHelper {

    public static void initialize(Context context) throws InterruptedException {
        TwilioConversationsClient.initialize(context);
    }

    public static void destroy() {
        if(TwilioConversationsClient.isInitialized()) {
            TwilioConversationsClient.destroy();
        }
        while(TwilioConversationsClient.isInitialized());
    }

}
