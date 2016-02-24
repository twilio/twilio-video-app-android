package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.utils.TwilioConversationsUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class ConversationsClientSendInviteTests {
    private static String TOKEN = "token";
    private static String PARTICIPANT = "janne";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    /**
     * We only teardown because not every test will want the sdk initialized
     */
    @After
    public void teardown() {
        if (TwilioConversationsUtils.isInitialized()) {
            TwilioConversationsUtils.destroyTwilioSDK();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotSendInviteWithNullParticipantSet() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        OutgoingInvite invite = conversationsClient.sendConversationInvite(null, localMedia, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotSendInviteWithNullLocalMedia() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        OutgoingInvite invite = conversationsClient.sendConversationInvite(participants, null, conversationCallback());
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotSendInviteWithNullConversationCallback() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);

        OutgoingInvite invite = conversationsClient.sendConversationInvite(participants, localMedia, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testTwilioCannotSendInviteWithEmptyParticipantSet() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();

        OutgoingInvite invite = conversationsClient.sendConversationInvite(participants, localMedia, conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwilioCannotSendInviteIfOneOfParticipantsIsNull() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add(null);

        OutgoingInvite invite = conversationsClient.sendConversationInvite(participants, localMedia, conversationCallback());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTwilioCannotSendInviteIfOneOfParticipantsIsEmptyString() {
        ConversationsClient conversationsClient = createConversationsClient();
        Assert.assertNotNull(conversationsClient);

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(localMediaListener());

        Set<String> participants = new HashSet<>();
        participants.add(PARTICIPANT);
        participants.add("");

        OutgoingInvite invite = conversationsClient.sendConversationInvite(participants, localMedia, conversationCallback());
    }

    private ConversationsClient createConversationsClient() {
        TwilioConversationsUtils.initializeTwilioSDK(mActivityRule.getActivity().getApplicationContext());
        ConversationsClient conversationsClient = TwilioConversations.createConversationsClient(TOKEN, conversationsClientListener());
        return conversationsClient;
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

    private ConversationListener conversationListener() {
        return new ConversationListener() {

            @Override
            public void onParticipantConnected(Conversation conversation, Participant participant) {

            }

            @Override
            public void onFailedToConnectParticipant(Conversation conversation, Participant participant, TwilioConversationsException e) {

            }

            @Override
            public void onParticipantDisconnected(Conversation conversation, Participant participant) {

            }

            @Override
            public void onConversationEnded(Conversation conversation, TwilioConversationsException e) {

            }
        };
    }

    private LocalMediaListener localMediaListener(){
        return new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {

            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {

            }
        };
    }

    private ConversationCallback conversationCallback() {
        return new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, TwilioConversationsException e) {

            }
        };
    }
}
