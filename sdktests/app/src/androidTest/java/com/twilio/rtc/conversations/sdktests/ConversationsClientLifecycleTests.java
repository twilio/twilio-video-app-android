package com.twilio.rtc.conversations.sdktests;

import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.rtc.conversations.sdktests.provider.TCCapabilityTokenProvider;
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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


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
            public void onLocalVideoTrackAdded(Conversation conversation, LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack localVideoTrack) {

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

    @Test
    public void muteShouldBeSafeToCallOnTheConversation() {
        final CountDownLatch initWait = new CountDownLatch(1);

        if(!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    initWait.countDown();
                }

                @Override
                public void onError(Exception e) {
                    org.junit.Assert.fail();
                }
            });
        } else {
            initWait.countDown();
        }
        TwilioConversationsUtils.wait(initWait, 5, TimeUnit.SECONDS);

        final String token = obtainCapabilityToken(PARTICIPANT);
        org.junit.Assert.assertNotNull(token);

        final CountDownLatch wait = new CountDownLatch(1);

        /*
         * The test thread cannot create new handlers. Use the main
         * thread to ensure we can receive callbacks on the ConversationsClient which
         * uses a handler to callback on the thread that created it.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(token, new ConversationsClientListener() {
                    @Override
                    public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(new LocalMediaListener() {
                            @Override
                            public void onLocalVideoTrackAdded(final Conversation conversation, LocalVideoTrack localVideoTrack) {
                               final Handler handler = new Handler();
                               handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        conversation.getLocalMedia().mute(!conversation.getLocalMedia().isMuted());
                                        handler.postDelayed(this, 100);
                                    }
                                });
                            }

                            @Override
                            public void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack localVideoTrack) {
                                // do nothing
                            }
                        });

                        CameraCapturer cameraCapturer = CameraCapturerFactory.createCameraCapturer(mActivityRule.getActivity(), CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null, new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {

                            }
                        });

                        localMedia.addLocalVideoTrack(LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer));

                        Set<String> participants = new HashSet<>();
                        participants.add("FOO");
                        conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, ConversationException e) {
                                org.junit.Assert.assertNotNull(e);
                                wait.countDown();
                            }
                        });

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
                conversationsClient.listen();
            }
        });

        TwilioConversationsUtils.wait(wait, 10, TimeUnit.SECONDS);
    }

    private String obtainCapabilityToken(final String username) {
        final CountDownLatch tokenLatch = new CountDownLatch(1);
        final String[] token = new String[1];
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(username, new Callback<String>() {

            @Override
            public void success(final String capabilityToken, Response response) {
                if (response.getStatus() == 200) {
                    token[0] = capabilityToken;
                    tokenLatch.countDown();
                } else {
                    org.junit.Assert.fail();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                org.junit.Assert.fail();
            }
        });

        TwilioConversationsUtils.wait(tokenLatch, 5, TimeUnit.SECONDS);
        return token[0];
    }

}
