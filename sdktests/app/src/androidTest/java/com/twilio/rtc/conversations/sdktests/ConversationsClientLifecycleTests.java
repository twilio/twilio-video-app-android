package com.twilio.rtc.conversations.sdktests;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.rtc.conversations.sdktests.utils.TwilioConversationsUtils;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.TwilioConversations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class ConversationsClientLifecycleTests {

    private static final String TEST_USER = "john";
    private static String TOKEN = "token";
    private static String PARTICIPANT = "janne";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void testTwilioCreateConversationsClientWithToken() {
        ConversationsClient conversationsClient = createConversationsClient();
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotListenAfterConversationsClientDisposal() {
        ConversationsClient conversationsClient = createConversationsClient();
        org.junit.Assert.assertNotNull(conversationsClient);

        conversationsClient.dispose();
        conversationsClient.listen();
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotUnlistenAfterConversationsClientDisposal() {
        ConversationsClient conversationsClient = createConversationsClient();
        org.junit.Assert.assertNotNull(conversationsClient);

        conversationsClient.dispose();
        conversationsClient.unlisten();
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotCreateConversationAfterConversationsClientDisposal() {
        ConversationsClient conversationsClient = createConversationsClient();
        org.junit.Assert.assertNotNull(conversationsClient);

        conversationsClient.dispose();
        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {

            }
        });
        OutgoingInvite outgoingInvite = conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, ConversationException e) {

            }
        });
    }

    @Test
    public void testTwilioMultiDisposeConversationsClient() {
        for (int i= 1; i < 50; i++) {
            ConversationsClient conversationsClient = createConversationsClient();
            org.junit.Assert.assertNotNull(conversationsClient);
            conversationsClient.dispose();
        }
    }

    private ConversationsClient createConversationsClient() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(TOKEN, conversationsClientListener());
        return conversationsClient;
    }

    @Test
    public void testTwilioFailsToListenWithDummyToken() {
        final CountDownLatch wait = new CountDownLatch(1);
        /*
         * The test thread cannot create new handlers. Use the main
         * thread to ensure we can receive callbacks on the ConversationsClient which
         * uses a handler to callback on the thread that created it.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(TOKEN, new ConversationsClientListener() {
                    @Override
                    public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                        org.junit.Assert.fail();
                    }

                    @Override
                    public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                        org.junit.Assert.fail();
                    }

                    @Override
                    public void onFailedToStartListening(ConversationsClient conversationsClient, ConversationException e) {
                        wait.countDown();
                    }

                    @Override
                    public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                        org.junit.Assert.fail();

                    }

                    @Override
                    public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                        org.junit.Assert.fail();
                    }

                });
                conversationsClient.listen();
            }
        });
        TwilioConversationsUtils.wait(wait, 10, TimeUnit.SECONDS);
    }

    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {

            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {

            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, ConversationException e) {

            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

        };
    }

}
