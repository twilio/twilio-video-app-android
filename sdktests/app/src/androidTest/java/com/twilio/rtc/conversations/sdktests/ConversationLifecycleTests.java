package com.twilio.rtc.conversations.sdktests;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.Participant;
import com.twilio.signal.TwilioRTC;
import com.twilio.signal.VideoTrack;

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
public class ConversationLifecycleTests {

    private static String CLIENT_NAME = "jtestic";

    private static String token = "";

    @Rule
    public ActivityTestRule<TwilioRTCActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCActivity.class);

    @Test
    public void testTwilioCreateConversation() {
        Endpoint endpoint = createEndpoint();

        /*
        Set<String> participants = new HashSet<String>();
        participants.add("joja");
        LocalMediaImpl localMedia = new LocalMediaImpl();
        CountDownLatch waitLatch = new CountDownLatch(1);
        Conversation conv = endpoint.createConversation(participants, localMedia, conversationListener(waitLatch));
        conv.disconnect();
        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);*/
    }

    private Endpoint createEndpoint() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        obtainCapabilityToken(CLIENT_NAME, waitLatch);
        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);

        waitLatch = new CountDownLatch(1);
        Endpoint endpoint = TwilioRTC.createEndpoint(token, endpointListener(waitLatch));
        TestTools.wait(waitLatch, 120, TimeUnit.SECONDS);
        return endpoint;

    }

    private void obtainCapabilityToken(final String username, final CountDownLatch waitLatch) {
        if (token != "") {
            waitLatch.countDown();
            return;
        }
        TCCapabilityTokenProvider.obtainTwilioCapabilityToken(username, new Callback<String>() {
            @Override
            public void success(String capabilityToken, Response response) {
                token = capabilityToken;
                waitLatch.countDown();
            }

            @Override
            public void failure(RetrofitError error) {
                org.junit.Assert.fail("failed to obtain token:"+error.getMessage());
            }
        });

    }

    private EndpointListener endpointListener(final CountDownLatch waitLatch) {
        return new EndpointListener() {
            @Override
            public void onStartListeningForInvites(Endpoint endpoint) {
                org.junit.Assert.fail("STAERTED LISTENING");
                waitLatch.countDown();
            }

            @Override
            public void onStopListeningForInvites(Endpoint endpoint) {
                System.out.print("");
            }

            @Override
            public void onFailedToStartListening(Endpoint endpoint, ConversationException e) {
                org.junit.Assert.fail("failed to listen for invites:" + e.getMessage());
            }

            @Override
            public void onReceiveConversationInvite(Endpoint endpoint, Invite invite) {
                System.out.print("");
            }
        };
    }

    private ConversationListener conversationListener(final CountDownLatch waitLatch) {
        return new ConversationListener() {
            @Override
            public void onConnectParticipant(Conversation conversation, Participant participant) {
                org.junit.Assert.fail("failed to listen for invites:");
            }

            @Override
            public void onFailToConnectParticipant(Conversation conversation, Participant participant, ConversationException e) {
                org.junit.Assert.fail("failed to listen for invites:");
            }

            @Override
            public void onDisconnectParticipant(Conversation conversation, Participant participant) {
                org.junit.Assert.fail("failed to listen for invites:");
            }

            @Override
            public void onVideoAddedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                org.junit.Assert.fail("failed to listen for invites:");
            }

            @Override
            public void onVideoRemovedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack) {
                org.junit.Assert.fail("failed to listen for invites:");
            }

            @Override
            public void onLocalStatusChanged(Conversation conversation, Conversation.Status status) {
                System.out.println("joja");
            }

            @Override
            public void onConversationEnded(Conversation conversation) {
                waitLatch.countDown();
            }

            @Override
            public void onConversationEnded(Conversation conversation, ConversationException e) {
                org.junit.Assert.fail("conversation ended with error:"+e.getMessage());
            }
        };
    }


}
