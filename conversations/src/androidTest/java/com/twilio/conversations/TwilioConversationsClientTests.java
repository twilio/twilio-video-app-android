package com.twilio.conversations;

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.TwilioConversationsClientHelper;
import com.twilio.conversations.helper.TwilioConversationsHelper;
import com.twilio.conversations.helper.TwilioConversationsTestsBase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TwilioConversationsClientTests extends TwilioConversationsTestsBase {
    private final static String TEST_USER = "TEST_USER";
    private static String PARTICIPANT = "janne";
    private Context context;

    private TwilioAccessManager accessManager;

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Before
    public void setup() {
        context = activityRule.getActivity();
    }

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
        if(accessManager != null) {
            accessManager.dispose();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void cannotSendInviteWithNullParticipantSet() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        LocalMedia localMedia = LocalMedia.create(localMediaListener());
        Assert.assertNotNull(localMedia);

        twilioConversationsClient.sendConversationInvite(null, localMedia, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotSendInviteWithNullLocalMedia() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        twilioConversationsClient.sendConversationInvite(participants, null, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void cannotSendInviteWithNullConversationCallback() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        LocalMedia localMedia = LocalMedia.create(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        twilioConversationsClient.sendConversationInvite(participants, localMedia, null);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void cannotSendInviteWithEmptyParticipantSet() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        LocalMedia localMedia = LocalMedia.create(localMediaListener());

        Set<String> participants = new HashSet<>();

        twilioConversationsClient.sendConversationInvite(participants,
                localMedia,
                conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannotSendInviteIfOneOfParticipantsIsNull() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        LocalMedia localMedia = LocalMedia.create(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add(null);

        twilioConversationsClient.sendConversationInvite(participants,
                localMedia,
                conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannotSendInviteIfOneOfParticipantsIsEmptyString() throws InterruptedException {
        accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, PARTICIPANT);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        Assert.assertNotNull(twilioConversationsClient);

        LocalMedia localMedia = LocalMedia.create(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add("");

        twilioConversationsClient.sendConversationInvite(participants, localMedia, conversationCallback());
    }

    @Test
    @Ignore
    public void canListenAfterClientCreation() throws InterruptedException {
        TwilioAccessManager accessManager = AccessTokenHelper.obtainTwilioAccessManager(context,
                TEST_USER);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper
                .registerClient(activityRule.getActivity(), accessManager);
        assertTrue(twilioConversationsClient.isListening());
    }

    private LocalMedia.Listener localMediaListener(){
        return new LocalMedia.Listener() {
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
