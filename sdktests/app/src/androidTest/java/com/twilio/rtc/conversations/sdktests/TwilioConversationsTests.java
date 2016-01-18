package com.twilio.rtc.conversations.sdktests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.rtc.conversations.sdktests.utils.TwilioConversationsUtils;
import com.twilio.conversations.ConversationException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversations;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class TwilioConversationsTests {


    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void shouldInitialize() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void canInitializeRepeatedly() {
        int attempts = 10;
        final CountDownLatch initLatch = TwilioConversationsUtils.isInitialized() ? new CountDownLatch(0) : new CountDownLatch(1);
        final CountDownLatch errorLatch = TwilioConversationsUtils.isInitialized() ? new CountDownLatch(attempts) : new CountDownLatch(attempts - 1);

        for(int i = 0; i < attempts; i++) {
            TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), TwilioConversationsUtils.countDownInitListenerCallback(initLatch, errorLatch));
        }

        try {
            initLatch.await(TwilioConversationsUtils.TIMEOUT, TimeUnit.SECONDS);
            errorLatch.await(TwilioConversationsUtils.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("test timed out after" + TwilioConversationsUtils.TIMEOUT);
        }

    }

    @Test
    public void canCreateConversationsClientWithNullParams() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        boolean npeSeen = false;

        try {
            TwilioConversations.createConversationsClient((String) null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient((TwilioAccessManager) null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient(null, null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioConversations.createConversationsClient("foo", null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient(
                    TwilioAccessManagerFactory.createAccessManager("foo", null), null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioConversations.createConversationsClient(null, null, conversationsClientListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            assertTrue(npeSeen);
            npeSeen = false;
        }

    }

    @Test
    public void canCreateConversationsClientWithToken() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient("DEADBEEF", conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        assertNotNull(conversationsClient);
    }

    @Test
    public void canCreateConversationsClientWithAccessManagerAndEmptyOptionsMap() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(accessManager, new HashMap<String, String>(), conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        assertNotNull(conversationsClient);
    }

    @Test
    public void canCreateConversationsClientWithAccessManagerAndRandomOption() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        HashMap optionsMap = new HashMap<String, String>();
        optionsMap.put("foo", "bar");
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(accessManager, optionsMap, conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        assertNotNull(conversationsClient);
    }

    @Test
    public void canSetAndGetLogLevel() {
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.DEBUG);
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.DISABLED);
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.ERROR);
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.INFO);
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.VERBOSE);
        setAndGetLogLevelShouldMatch(TwilioConversations.LogLevel.WARNING);
    }

    private void setAndGetLogLevelShouldMatch(int level) {
        TwilioConversations.setLogLevel(level);
        assertEquals(level, TwilioConversations.getLogLevel());
    }

    @Test
    public void shouldSetDisabledIfInvalidLogLevelProvided() {
        int invalidLevel = 100;
        TwilioConversations.setLogLevel(invalidLevel);
        assertEquals(TwilioConversations.LogLevel.DISABLED, TwilioConversations.getLogLevel());
    }

    @Test
    public void canGetVersion() {
        String version = TwilioConversations.getVersion();
        assertNotNull(version);
    }

    @Test
    public void versionShouldBeSemVer() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = TwilioConversations.getVersion();
        assertTrue(version.matches(semVerRegex));
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
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

        };
    }

}
