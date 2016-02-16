package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.provider.TCCapabilityTokenProvider;
import com.twilio.conversations.utils.TwilioConversationsUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class ListenTest {
    private final static String TEST_USER = "TEST_USER";

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
    public void canListenAfterClientCreation() {
        final CountDownLatch waitLatch = new CountDownLatch(1);
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(TEST_USER, new Callback<String>() {
            @Override
            public void success(final String token, Response response) {

                TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), new TwilioConversations.InitListener() {
                    @Override
                    public void onInitialized() {
                        TwilioAccessManagerFactory.createAccessManager(token, new TwilioAccessManagerListener() {
                                    @Override
                                    public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                                        fail();
                                    }

                                    @Override
                                    public void onTokenUpdated(final TwilioAccessManager twilioAccessManager) {
                                        assertNotNull(twilioAccessManager);
                                        assertEquals(token, twilioAccessManager.getToken());

                                        ConversationsClient client = TwilioConversations.createConversationsClient(twilioAccessManager, new ConversationsClientListener() {
                                            @Override
                                            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                                                waitLatch.countDown();
                                            }

                                            @Override
                                            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                                                fail();
                                            }

                                            @Override
                                            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException e) {
                                                fail();
                                            }

                                            @Override
                                            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                                                fail();
                                            }

                                            @Override
                                            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                                                fail();
                                            }
                                        });
                                        assertNotNull(client);
                                        client.listen();
                                    }

                                    @Override
                                    public void onError (TwilioAccessManager twilioAccessManager, String s){
                                        fail();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        fail(e.getMessage());
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                fail(error.getMessage());
            }
        });

        TwilioConversationsUtils.wait(waitLatch, 30, TimeUnit.SECONDS);
    }

}
