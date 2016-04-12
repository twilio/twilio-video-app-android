package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConversationsClientHelper {

    public static ConversationsClient registerClient(Context context, TwilioAccessManager twilioAccessManager) throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        final CountDownLatch listeningLatch = new CountDownLatch(1);

        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(twilioAccessManager, new ConversationsClientListener() {

            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                listeningLatch.countDown();
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {

            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException exception) {
                fail();
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                fail();
            }
        });

        conversationsClient.listen();
        assertTrue(listeningLatch.await(10, TimeUnit.SECONDS));
        return conversationsClient;
    }

}
