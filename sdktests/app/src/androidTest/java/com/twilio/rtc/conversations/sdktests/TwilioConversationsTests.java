package com.twilio.rtc.conversations.sdktests;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.rtc.conversations.sdktests.utils.TwilioConversationsUtils;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversations;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class TwilioConversationsTests {
    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

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
    public void testTwilioInitialize() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void testTwilioDestroy() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        TwilioConversationsUtils.destroyTwilioSDK();
    }

    @Test
    public void testTwilioDestroyWithActiveClient() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        TwilioConversations.createConversationsClient("token",
                        conversationsClientListener());
        TwilioConversationsUtils.destroyTwilioSDK();
    }

    @Test
    public void testTwilioDestroyWithDisposingClient() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        ConversationsClient conversationsClient =
                TwilioConversations.createConversationsClient("token",
                        conversationsClientListener());
        conversationsClient.dispose();
        TwilioConversations.destroy();
    }

    @Test
    public void testTwilioInitializationAfterDestroy() {
        final CountDownLatch initLatch = TwilioConversationsUtils.isInitialized() ? new CountDownLatch(0) : new CountDownLatch(1);
        TwilioConversations.initialize(mActivityRule.getActivity().getApplicationContext(), TwilioConversationsUtils.countDownInitListenerCallback(initLatch, new CountDownLatch(1)));
        try {
            initLatch.await(TwilioConversationsUtils.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TwilioConversationsUtils.TIMEOUT);
        }
        TwilioConversations.destroy();
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
    }

    @Test
    public void testTwilioInitializeRepeatedly() {
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
            org.junit.Assert.fail("test timed out after" + TwilioConversationsUtils.TIMEOUT);
        }
    }

    @Test
    public void testClientCreationBeforeInitialize() {
        exception.expect(IllegalStateException.class);
        String bogusToken = "1234";
        TwilioConversations.createConversationsClient(bogusToken,
                new ConversationsClientListener() {
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
        });
    }

    @Test
    public void testTwilioCreateConversationsClientWithNullParams() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        boolean npeSeen = false;

        try {
            TwilioConversations.createConversationsClient((String) null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient((TwilioAccessManager) null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient(null, null, null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioConversations.createConversationsClient("foo", null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }

        try {
            TwilioConversations.createConversationsClient(
                    TwilioAccessManagerFactory.createAccessManager("foo", null), null);
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }


        try {
            TwilioConversations.createConversationsClient(null, null, conversationsClientListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
            npeSeen = false;
        }
    }

    @Test
    public void testTwilioCreateConversationsClientWithToken() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient("DEADBEEF", conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndEmptyOptionsMap() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(accessManager, new HashMap<String, String>(), conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndRandomOption() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity());

        CountDownLatch waitLatch = new CountDownLatch(1);
        HashMap optionsMap = new HashMap<String, String>();
        optionsMap.put("foo", "bar");
        TwilioAccessManager accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(accessManager, optionsMap, conversationsClientListener());

        // TODO: check start listening once callback issue is resolved
        org.junit.Assert.assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioSetAndGetLogLevel() {
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.DEBUG);
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.DISABLED);
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.ERROR);
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.INFO);
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.VERBOSE);
        verifySetAndGetLogLevel(TwilioConversations.LogLevel.WARNING);
    }

    @Test
    public void testTwilioEnsureInvalidLevelSetsLevelToDisabled() {
        int invalidLevel = 100;
        TwilioConversations.setLogLevel(invalidLevel);
        org.junit.Assert.assertEquals(TwilioConversations.LogLevel.DISABLED, TwilioConversations.getLogLevel());
    }

    @Test
    public void testTwilioEnsureLogLevelSetBeforeAndAfterInit() {
        // TODO: implement me
    }

    @Test
    public void testTwilioGetVersion() {
        String version = TwilioConversations.getVersion();
        org.junit.Assert.assertNotNull(version);
    }

    @Test
    public void testTwilioVersionUsesSemanticVersioning() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = TwilioConversations.getVersion();
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

    private void verifySetAndGetLogLevel(int level) {
        TwilioConversations.setLogLevel(level);
        org.junit.Assert.assertEquals(level, TwilioConversations.getLogLevel());
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
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException e) {

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
