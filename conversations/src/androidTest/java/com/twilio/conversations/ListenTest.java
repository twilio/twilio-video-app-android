package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.ConversationsClientHelper;
import com.twilio.conversations.helper.TwilioConversationsHelper;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class ListenTest {
    private final static String TEST_USER = "TEST_USER";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
    }

    @Test
    public void canListenAfterClientCreation() throws InterruptedException {
        TwilioAccessManager accessManager = AccessTokenHelper.obtainTwilioAccessManager(TEST_USER);
        ConversationsClient conversationsClient = ConversationsClientHelper.registerClient(mActivityRule.getActivity(), accessManager);
        assertTrue(conversationsClient.isListening());
    }

}
