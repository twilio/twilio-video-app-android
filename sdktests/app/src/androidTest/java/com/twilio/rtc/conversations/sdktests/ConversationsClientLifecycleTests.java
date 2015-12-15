package com.twilio.rtc.conversations.sdktests;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.rtc.conversations.sdktests.utils.TwilioConversationsUtils;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.ConversationsClient;
import com.twilio.signal.ConversationsClientListener;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.MediaFactory;
import com.twilio.signal.Participant;
import com.twilio.signal.TwilioConversations;
import com.twilio.signal.VideoTrack;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class ConversationsClientLifecycleTests {

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
        LocalMedia localMedia = MediaFactory.createLocalMedia();
        Conversation conv = conversationsClient.createConversation(participants, localMedia, conversationListener());
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
                    public void onReceiveConversationInvite(ConversationsClient conversationsClient, Invite invite) {
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
            public void onReceiveConversationInvite(ConversationsClient conversationsClient, Invite invite) {

            }
        };
    }

    private ConversationListener conversationListener() {
        return new ConversationListener() {

            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {

            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e) {

            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {

            }

            @Override
            public void onLocalVideoAdded(Conversation conversation, LocalVideoTrack videoTrack) {
                
            }

            @Override
            public void onLocalVideoRemoved(Conversation conversation, LocalVideoTrack videoTrack) {

            }

            @Override
            public void onVideoAddedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack) {

            }

            @Override
            public void onVideoRemovedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack) {

            }

            @Override
            public void onLocalStatusChanged(Conversation conversation, Conversation.Status status) {

            }

            @Override
            public void onConversationEnded(Conversation conversation, ConversationException e) {

            }
        };
    }

}
