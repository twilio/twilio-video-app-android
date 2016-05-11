package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversationsException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TwilioConversationsClientHelper {

    public static TwilioConversationsClient registerClient(Context context, TwilioAccessManager twilioAccessManager) throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        final CountDownLatch listeningLatch = new CountDownLatch(1);

        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClient.create(twilioAccessManager, new TwilioConversationsClient.Listener() {

            @Override
            public void onStartListeningForInvites(TwilioConversationsClient conversationsClient) {
                listeningLatch.countDown();
            }

            @Override
            public void onStopListeningForInvites(TwilioConversationsClient conversationsClient) {

            }

            @Override
            public void onFailedToStartListening(TwilioConversationsClient conversationsClient, TwilioConversationsException exception) {
                fail();
            }

            @Override
            public void onIncomingInvite(TwilioConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(TwilioConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                fail();
            }
        });

        twilioConversationsClient.listen();
        assertTrue(listeningLatch.await(10, TimeUnit.SECONDS));
        return twilioConversationsClient;
    }

}
