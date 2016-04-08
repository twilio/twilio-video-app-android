package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;

import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.TwilioConversationsHelper;
import com.twilio.conversations.internal.TwilioConversationsInternal;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@Ignore
public class TwilioConversationsTest {
    private TwilioAccessManager accessManager;

    @Rule
    public final ActivityTestRule<TwilioConversationsActivity> mActivityRule =
            new ActivityTestRule<>(TwilioConversationsActivity.class);

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
        if(accessManager != null) {
            accessManager.dispose();
        }
    }

    @Test
    public void testTwilioInitialize() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());
    }

    @Test
    public void testTwilioDestroyWithActiveClient() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());
        TwilioConversations.createConversationsClient("token",
                conversationsClientListener());
    }

    @Test
    public void testTwilioInitializationAfterDestroy() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());
        TwilioConversationsHelper.destroy();
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());
    }

    @Test(expected =  IllegalStateException.class)
    public void testClientCreationBeforeInitialize() {
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
    public void testTwilioCreateConversationsClientWithNullParams() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());

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
            TwilioConversationsInternal.createConversationsClient(null, null, null);
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
            TwilioConversationsInternal.createConversationsClient(null, null, conversationsClientListener());
        } catch(NullPointerException e) {
            npeSeen = true;
        } finally {
            org.junit.Assert.assertTrue(npeSeen);
        }
    }

    @Test
    public void testTwilioCreateConversationsClientWithToken() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());

        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient("DEADBEEF", conversationsClientListener());

        assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndEmptyOptionsMap() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());

        accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversationsInternal.createConversationsClient(accessManager, new HashMap<String, String>(), conversationsClientListener());

        assertNotNull(conversationsClient);
    }

    @Test
    public void testTwilioCreateConversationsClientWithAccessManagerAndRandomOption() throws InterruptedException {
        TwilioConversationsHelper.initialize(mActivityRule.getActivity());

        HashMap optionsMap = new HashMap<>();
        optionsMap.put("foo", "bar");
        accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversationsInternal.createConversationsClient(accessManager, optionsMap, conversationsClientListener());

        assertNotNull(conversationsClient);
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
        assertNotNull(version);
    }

    @Test
    public void testTwilioVersionUsesSemanticVersioning() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = TwilioConversations.getVersion();
        org.junit.Assert.assertTrue(version.matches(semVerRegex));
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
        };
    }
}
