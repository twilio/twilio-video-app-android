package com.twilio.rtc.conversations.sdktests;

import android.os.Build;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.TwilioConversations;
import com.twilio.rtc.conversations.sdktests.provider.TCCapabilityTokenProvider;
import com.twilio.rtc.conversations.sdktests.utils.TwilioConversationsUtils;

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

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ConversationTests {

    private static String USER = "john";
    private static String NON_EXISTANT_PARTICIPANT ="non-existant-paritcipant";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void muteShouldBeSafeToCallAnytimeDuringAConversation() {
        // TODO: support enabling runtime permissions on Android 6.0+
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
           return;
        }

        final CountDownLatch initWait = new CountDownLatch(1);

        if(!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    initWait.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail(e.getMessage());
                }
            });
        } else {
            initWait.countDown();
        }
        TwilioConversationsUtils.wait(initWait, 5, TimeUnit.SECONDS);

        // Intentionally obtaining the token with a latch to remain on the test thread.
        final String token = obtainCapabilityToken(USER);
        assertNotNull(token);

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
                            public void onLocalVideoTrackAdded(final LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                                final Handler handler = new Handler();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        localMedia.mute(!localMedia.isMuted());
                                        handler.postDelayed(this, 100);
                                    }
                                });
                            }

                            @Override
                            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                                // do nothing
                            }

                            @Override
                            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {

                            }
                        });

                        CameraCapturer cameraCapturer = CameraCapturerFactory.createCameraCapturer(mActivityRule.getActivity(), CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null, new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {

                            }
                        });

                        localMedia.addLocalVideoTrack(LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer));

                        Set<String> participants = new HashSet<>();
                        participants.add(NON_EXISTANT_PARTICIPANT);
                        conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, TwilioConversationsException e) {
                                assertNotNull(e);
                                wait.countDown();
                            }
                        });

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
                    fail();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                fail(error.getMessage());
            }
        });

        TwilioConversationsUtils.wait(tokenLatch, 5, TimeUnit.SECONDS);
        return token[0];
    }

}
