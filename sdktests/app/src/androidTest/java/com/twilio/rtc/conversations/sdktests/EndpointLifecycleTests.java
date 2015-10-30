package com.twilio.rtc.conversations.sdktests;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.ConversationException;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.TwilioRTC;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EndpointLifecycleTests {

    private static String token = "jtestic";

    @Rule
    public ActivityTestRule<TwilioRTCActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCActivity.class);

    @Test
    public void testTwilioCreateEndpointWithToken() {
        Endpoint endpoint = createEndpoint();
        org.junit.Assert.assertNotNull(endpoint);
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioDisposeEndpointCallListen() {
        Endpoint endpoint = createEndpoint();
        org.junit.Assert.assertNotNull(endpoint);

        endpoint.dispose();
        endpoint.listen();

    }

    private Endpoint createEndpoint() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        Endpoint endpoint = TwilioRTC.createEndpoint(token, endpointListener());
        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);
        return endpoint;

    }

    private EndpointListener endpointListener() {
        return new EndpointListener() {
            @Override
            public void onStartListeningForInvites(Endpoint endpoint) {

            }

            @Override
            public void onStopListeningForInvites(Endpoint endpoint) {

            }

            @Override
            public void onFailedToStartListening(Endpoint endpoint, ConversationException e) {

            }

            @Override
            public void onReceiveConversationInvite(Endpoint endpoint, Invite invite) {

            }
        };
    }

}
