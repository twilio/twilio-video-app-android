package com.twilio.conversations;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.conversations.helper.TwilioConversationsHelper;
import com.twilio.conversations.impl.ConversationsClientImpl;
import com.twilio.conversations.internal.ClientOptionsInternal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TwilioConversationsTests {
    private TwilioAccessManager accessManager;

    private Context context;

    @BeforeClass
    public static void suiteSetup() {
        TwilioConversationsHelper.destroy();
    }

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
        if(accessManager != null) {
            accessManager.dispose();
        }
    }

    @AfterClass
    public static void suiteTeardown() {
        TwilioConversationsHelper.destroy();
    }

    @Test(expected = NullPointerException.class)
    public void initialize_shouldNotAllowNullContext() {
        TwilioConversations.initialize(null,
                TwilioConversationsHelper.createInitListener());
    }

    @Test(expected = NullPointerException.class)
    public void initialize_shouldNotAllowNullInitListener() {
        TwilioConversations.initialize(context, null);
    }

    @Test
    public void initialize_shouldSucceedWithValidContextAndListener() throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        assertTrue(TwilioConversations.isInitialized());
    }

    @Test
    public void initialize_shouldNotifyWithErrorWhenAllreadyInitialized()
            throws InterruptedException {
        final CountDownLatch errorCallback = new CountDownLatch(1);
        TwilioConversationsHelper.initialize(context);

        TwilioConversations.initialize(context, new TwilioConversations.InitListener() {
            @Override
            public void onInitialized() {
                fail("Should receive error because sdk is initialized all ready!");
            }

            @Override
            public void onError(Exception exception) {
                errorCallback.countDown();
            }
        });
        assertTrue(errorCallback.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void destroy_shouldSucceedAfterInitilization() throws InterruptedException {
        TwilioConversationsHelper.initialize(context);
        TwilioConversationsHelper.destroy();
    }

    @Test
    public void initialize_shouldWorkRepeatidelyAfterDestroy() throws InterruptedException {
        for (int i = 0 ; i < 10 ; i++) {
            TwilioConversationsHelper.initialize(context);
            assertTrue(TwilioConversations.isInitialized());
            TwilioConversationsHelper.destroy();
            assertFalse(TwilioConversations.isInitialized());
        }
    }

    /**
     * TODO
     * Add way to validate this test using reflection on conversations client
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void destroy_shouldDestroyActiveConversationClients() throws InterruptedException {
        TwilioConversationsHelper.initialize(context);
        ConversationsClientImpl conversationClient = (ConversationsClientImpl) TwilioConversations
                .createConversationsClient("token",
                        conversationsClientListener());
    }

    @Test(expected =  IllegalStateException.class)
    public void createConversationsClient_shouldBeAllowedBeforeInitialize() {
        TwilioConversations.createConversationsClient("bogus token", conversationsClientListener());
    }

    @Test(expected = NullPointerException.class)
    public void createConversationsClient_shouldNowAllowNullAcessToken()
            throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        TwilioConversations.createConversationsClient((String) null, conversationsClientListener());
    }

    @Test(expected = NullPointerException.class)
    public void createConversationsClient_shouldNotAllowNullAccessManager()
            throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        TwilioConversations.createConversationsClient(null,
                new ClientOptions(),
                conversationsClientListener());
    }

    @Test
    public void createConversationsClient_shouldReturnClientForValidToken()
            throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        ConversationsClient conversationsClient = TwilioConversations
                .createConversationsClient("DEADBEEF", conversationsClientListener());

        assertNotNull(conversationsClient);
    }

    @Test
    public void createConversationsClient_shouldAllowEmptyOptions()
            throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations
                .createConversationsClient(accessManager, null, conversationsClientListener());

        assertNotNull(conversationsClient);
    }

    @Test
    public void createConversationsClient_shouldAllowRandomOptions()
            throws InterruptedException {
        TwilioConversationsHelper.initialize(context);

        HashMap optionsMap = new HashMap<>();
        optionsMap.put("foo", "bar");
        ClientOptionsInternal options = new ClientOptionsInternal(optionsMap);
        accessManager = TwilioAccessManagerFactory.createAccessManager("DEADBEEF", null);
        ConversationsClient conversationsClient = TwilioConversations
                .createConversationsClient(accessManager, options, conversationsClientListener());

        assertNotNull(conversationsClient);
    }

    @Test
    public void setLogLevel_shouldSetLogLevelToDisabledForInvalidLevel() {
        int invalidLevel = 100;
        TwilioConversations.setLogLevel(invalidLevel);

        assertEquals(TwilioConversations.LogLevel.DISABLED,
                TwilioConversations.getLogLevel());
    }

    @Test
    public void setLogLevel_canBeDoneBeforeAndAfterInit() throws InterruptedException {
        int level = TwilioConversations.LogLevel.VERBOSE;

        TwilioConversations.setLogLevel(level);
        assertEquals(level, TwilioConversations.getLogLevel());

        TwilioConversationsHelper.initialize(context);

        level = TwilioConversations.LogLevel.ERROR;
        TwilioConversations.setLogLevel(level);
        assertEquals(level, TwilioConversations.getLogLevel());
    }

    @Test
    public void getVersion_shouldReturnValidSemVerFormattedVersion() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = TwilioConversations.getVersion();

        assertNotNull(version);
        assertNotEquals("", version);
        assertTrue(version.matches(semVerRegex));
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
