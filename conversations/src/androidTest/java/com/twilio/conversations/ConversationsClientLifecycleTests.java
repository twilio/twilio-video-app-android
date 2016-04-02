package com.twilio.conversations;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.utils.TwilioConversationsUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class ConversationsClientLifecycleTests {
    private static String TOKEN = "token";
    private static String PARTICIPANT = "janne";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    /**
     * We only teardown because not every test will want the sdk initialized
     */
    @After
    public void teardown() {
        if (TwilioConversationsUtils.isInitialized()) {
            TwilioConversationsUtils.destroyTwilioSDK();
        }
    }

    @Test
    public void testTwilioCreateConversationsClientWithToken() {
        ConversationsClient conversationsClient = createConversationsClient();
        assertNotNull(conversationsClient);
    }

    private ConversationsClient createConversationsClient() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(TOKEN, conversationsClientListener());
        return conversationsClient;
    }

    @Test
    public void testTwilioFailsToListenWithDummyToken() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());

        final CountDownLatch wait = new CountDownLatch(1);
        /*
         * The test thread cannot create new handlers. Use the main
         * thread to ensure we can receive callbacks on the ConversationsClient which
         * uses a handler to callback on the thread that created it.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {

                ConversationsClient conversationsClient = TwilioConversations
                        .createConversationsClient(TOKEN, new ConversationsClientListener() {
                            @Override
                            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                                fail();
                            }

                            @Override
                            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                                fail();
                            }

                            @Override
                            public void onFailedToStartListening(ConversationsClient conversationsClient,
                                                                 TwilioConversationsException e) {
                                wait.countDown();
                            }

                            @Override
                            public void onIncomingInvite(ConversationsClient conversationsClient,
                                                         IncomingInvite incomingInvite) {
                                fail();
                            }

                            @Override
                            public void onIncomingInviteCancelled(ConversationsClient conversationsClient,
                                                                  IncomingInvite incomingInvite) {
                                fail();
                            }
                        });
                conversationsClient.listen();
            }
        });

        TwilioConversationsUtils.wait(wait, 30, TimeUnit.SECONDS);
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
            public void onFailedToStartListening(ConversationsClient conversationsClient,
                                                 TwilioConversationsException e) {

            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient,
                                         IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient,
                                                  IncomingInvite incomingInvite) {

            }
        };
    }
}
