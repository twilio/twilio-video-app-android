package com.twilio.rtc.conversations.sdktests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.rtc.conversations.sdktests.utils.TwilioRTCUtils;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationsClient;
import com.twilio.signal.ConversationsClientListener;
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
        TwilioRTCUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void testTwilioInitializeRepeatedly() {
        int attempts = 10;
        final CountDownLatch initLatch = TwilioRTCUtils.isInitialized() ? new CountDownLatch(0) : new CountDownLatch(1);
        final CountDownLatch errorLatch = TwilioRTCUtils.isInitialized() ? new CountDownLatch(attempts) : new CountDownLatch(attempts - 1);

        for(int i = 0; i < attempts; i++) {
            TwilioRTC.initialize(mActivityRule.getActivity().getApplicationContext(), TwilioRTCUtils.countDownInitListenerCallback(initLatch, errorLatch));
        }

        try {
            initLatch.await(TwilioRTCUtils.TIMEOUT, TimeUnit.SECONDS);
            errorLatch.await(TwilioRTCUtils.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TwilioRTCUtils.TIMEOUT);
        }

    }



    @Test
    public void testTwilioCreateConversationsClientWithNullParams() {
        TwilioRTCUtils.initializeTwilioSDK(mActivityRule.getActivity());

        boolean npeSeen = false;

        try {
            TwilioRTC.createConversationsClient((String)null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createConversationsClient((TwilioAccessManager)null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createConversationsClient(null, null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioRTC.createConversationsClient("foo", null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioRTC.createConversationsClient(
                    TwilioAccessManagerFactory.createAccessManager("foo", null), null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioRTC.createConversationsClient(null, null, conversationsClientListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

    }

    @Test
    public void testTwilioCreateConversationsClientWithToken() {
        TwilioRTCUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        ConversationsClient conversationsClient = TwilioRTC.createConversationsClient("DEADBEEF", conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndEmptyOptionsMap() {
        TwilioRTCUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioRTC.createConversationsClient(accessManager, new HashMap<String, String>(), conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndRandomOption() {
        TwilioRTCUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        HashMap optionsMap = new HashMap<String, String>();
        optionsMap.put("foo", "bar");
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioRTC.createConversationsClient(accessManager, optionsMap, conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
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

}
