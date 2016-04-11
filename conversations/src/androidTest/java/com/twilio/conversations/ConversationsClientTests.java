package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.ConversationsClientHelper;
import com.twilio.conversations.helper.TwilioConversationsHelper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConversationsClientTests {
    private final static String TEST_USER = "TEST_USER";
    private static String PARTICIPANT = "janne";

    private TwilioAccessManager accessManager;

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @BeforeClass
    public static void suiteSetup() {
        TwilioConversationsHelper.destroy();
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

    @Test(expected = IllegalStateException.class)
    public void cannotSendInviteWithNullParticipantSet() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());
        Assert.assertNotNull(localMedia);

        conversationsClient.sendConversationInvite(null, localMedia, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotSendInviteWithNullLocalMedia() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        conversationsClient.sendConversationInvite(participants, null, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void cannotSendInviteWithNullConversationCallback() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        conversationsClient.sendConversationInvite(participants, localMedia, null);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void cannotSendInviteWithEmptyParticipantSet() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();

        conversationsClient.sendConversationInvite(participants,
                localMedia,
                conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannotSendInviteIfOneOfParticipantsIsNull() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add(null);

        conversationsClient.sendConversationInvite(participants,
                localMedia,
                conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSendInviteIfOneOfParticipantsIsEmptyString() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(PARTICIPANT);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add("");

        conversationsClient.sendConversationInvite(participants, localMedia, conversationCallback());
    }

    @Test
    @Ignore
    public void canListenAfterClientCreation() throws InterruptedException {
        TwilioAccessManager accessManager = AccessTokenHelper.obtainTwilioAccessManager(TEST_USER);
        ConversationsClient conversationsClient = ConversationsClientHelper
                .registerClient(mActivityRule.getActivity(), accessManager);
        assertTrue(conversationsClient.isListening());
    }

    private LocalMediaListener localMediaListener(){
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia,
                                               LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia,
                                                 LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia,
                                               LocalVideoTrack localVideoTrack,
                                               TwilioConversationsException e) {

            }
        };
    }

    private ConversationCallback conversationCallback() {
        return new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation,
                                       TwilioConversationsException e) {
            }
        };
    }
}
