package com.twilio.rtc.conversations.sdktests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.ConversationException;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.TwilioRTC;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class TwilioRTCTests {


    @Rule
    public ActivityTestRule<TwilioRTCActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCActivity.class);

    @Test
    public void testTwilioInitialize() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void testTwilioInitializeRepeatedly() {
        int attempts = 10;
        final CountDownLatch initLatch = TestTools.isInitialized() ? new CountDownLatch(0) : new CountDownLatch(1);
        final CountDownLatch errorLatch = TestTools.isInitialized() ? new CountDownLatch(attempts) : new CountDownLatch(attempts - 1);

        for(int i = 0; i < attempts; i++) {
            TwilioRTC.initialize(mActivityRule.getActivity().getApplicationContext(), TestTools.countDownInitListenerCallback(initLatch, errorLatch));
        }

        try {
            initLatch.await(TestTools.TIMEOUT, TimeUnit.SECONDS);
            errorLatch.await(TestTools.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TestTools.TIMEOUT);
        }

    }



    @Test
    public void testTwilioCreateEndpointWithNullParams() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        boolean npeSeen = false;

        try {
            TwilioRTC.createEndpoint(null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createEndpoint("foo", null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createEndpoint(null, endpointListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createEndpoint("foo", null, endpointListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

    }

    @Test
    public void testTwilioCreateEndpointWithToken() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        Endpoint endpoint = TwilioRTC.createEndpoint("DEADBEEF", endpointListener());

        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);
        org.junit.Assert.assertNotNull(endpoint);
    }

    @Test
    public void testTwilioCreateEndpointWithTokenAndEmptyOptionsMap() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        Endpoint endpoint = TwilioRTC.createEndpoint("DEADBEEF", new HashMap<String, String>(), endpointListener());

        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);
        org.junit.Assert.assertNotNull(endpoint);
    }

    @Test
    public void testTwilioCreateEndpointWithTokenAndRandomOption() {
        TestTools.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        HashMap optionsMap = new HashMap<String, String>();
        optionsMap.put("foo", "bar");
        Endpoint endpoint = TwilioRTC.createEndpoint("DEADBEEF", optionsMap, endpointListener());

        TestTools.wait(waitLatch, TestTools.TIMEOUT, TimeUnit.SECONDS);
        org.junit.Assert.assertNotNull(endpoint);
    }

    @Test
    public void testTwilioSetAndGetLogLevel() {
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.DEBUG);
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.DISABLED);
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.ERROR);
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.INFO);
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.VERBOSE);
        verifySetAndGetLogLevel(TwilioRTC.LogLevel.WARNING);
    }

    private void verifySetAndGetLogLevel(int level) {
        TwilioRTC.setLogLevel(level);
        org.junit.Assert.assertEquals(level, TwilioRTC.getLogLevel());
    }

    @Test
    public void testTwilioEnsureInvalidLevelSetsLevelToDisabled() {
        int invalidLevel = 100;
        TwilioRTC.setLogLevel(invalidLevel);
        org.junit.Assert.assertEquals(TwilioRTC.LogLevel.DISABLED, TwilioRTC.getLogLevel());
    }

    @Test
    public void testTwilioEnsureLogLevelSetBeforeAndAfterInit() {
        // TODO: implement me
    }

    @Test
    public void testTwilioGetVersion() {
        String version = TwilioRTC.getVersion();
        org.junit.Assert.assertNotNull(version);
    }

    @Test
    public void testTwilioVersionUsesSemanticVersioning() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = TwilioRTC.getVersion();
        org.junit.Assert.assertTrue(version.matches(semVerRegex));
    }

    @Test
    public void testSetSpeakerphoneOff() {
        // TODO: validate speakerphone is off
    }

    @Test
    public void testSetSpeakerphoneOn() {
        // TODO: validate speakerphone is on
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
