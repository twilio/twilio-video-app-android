package com.twilio.conversations.listen;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.listen.provider.TCCapabilityTokenProvider;
import com.twilio.conversations.listen.utils.TwilioConversationsUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


@RunWith(AndroidJUnit4.class)
public class ListenTest {


    @Rule
    public ActivityTestRule<TwilioListenActivity> mActivityRule = new ActivityTestRule<>(
            TwilioListenActivity.class);

    @Test
    public void testListenRightAfterClientCreation() {
        final CountDownLatch waitLatch = new CountDownLatch(1);
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken("TEST", new Callback<String>() {
            @Override
            public void success(final String token, Response response) {

                TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), new TwilioConversations.InitListener() {
                    @Override
                    public void onInitialized() {
                        TwilioAccessManagerFactory.createAccessManager(token, new TwilioAccessManagerListener() {
                                    @Override
                                    public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                                        org.junit.Assert.fail();
                                    }

                                    @Override
                                    public void onTokenUpdated(final TwilioAccessManager twilioAccessManager) {
                                        org.junit.Assert.assertNotNull(twilioAccessManager);
                                        org.junit.Assert.assertEquals(token, twilioAccessManager.getToken());

                                        ConversationsClient client = TwilioConversations.createConversationsClient(twilioAccessManager, new ConversationsClientListener() {
                                            @Override
                                            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                                                waitLatch.countDown();
                                            }

                                            @Override
                                            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                                                org.junit.Assert.fail();
                                            }

                                            @Override
                                            public void onFailedToStartListening(ConversationsClient conversationsClient, ConversationException e) {
                                                org.junit.Assert.fail();
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
                                        org.junit.Assert.assertNotNull(client);
                                        client.listen();
                                    }

                                    @Override
                                    public void onError (TwilioAccessManager twilioAccessManager, String s){
                                        org.junit.Assert.fail();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        org.junit.Assert.fail();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                org.junit.Assert.fail();
            }
        });

        TwilioConversationsUtils.wait(waitLatch, 30, TimeUnit.SECONDS);
    }

}
