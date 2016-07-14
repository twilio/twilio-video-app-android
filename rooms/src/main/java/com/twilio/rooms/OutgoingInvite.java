package com.twilio.rooms;

import com.twilio.rooms.internal.Logger;

import java.util.Set;

/**
 * An OutgoingInvite represents an invitation to a list of {@link Participant} to start a conversation.
 *
 */
public class OutgoingInvite {
    static final Logger logger = Logger.getLogger(OutgoingInvite.class);

    private TwilioConversationsClientInternal conversationsClient;
    private Conversation conversation;
    private ConversationCallback conversationCallback;
    private InviteStatus inviteStatus;

    private OutgoingInvite(TwilioConversationsClientInternal conversationsClient,
                           Conversation conversation,
                           ConversationCallback conversationCallback) {
        this.conversation = conversation;
        this.conversationsClient = conversationsClient;
        this.conversationCallback = conversationCallback;
        this.inviteStatus = InviteStatus.PENDING;
    }

    static OutgoingInvite create(TwilioConversationsClientInternal conversationsClient,
                                 Conversation conversation,
                                 ConversationCallback conversationCallback) {
        if(conversationsClient == null) {
            return null;
        }
        if(conversation == null) {
            return null;
        }
        if(conversationCallback == null) {
            return null;
        }
        return new OutgoingInvite(conversationsClient, conversation, conversationCallback);
    }

    void setStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    Conversation getConversation() {
        return conversation;
    }

    ConversationCallback getConversationCallback() {
        return conversationCallback;
    }

    /**
     * Cancels this invitation
     */
    public void cancel() {
        if(inviteStatus == InviteStatus.PENDING) {
            logger.i("Cancelling pending invite");
            inviteStatus = InviteStatus.CANCELLED;
            conversation.disconnect();
        } else {
            logger.w("The invite was not cancelled. Invites can only be cancelled in the pending state");
        }
    }

    /**
     * Returns the identities of the participants invited to the conversation
     *
     * @return list of participant identities invited to conversation
     */
    public Set<String> getInvitedParticipants() {
        return conversation.getInvitedParticipants();
    }

    /**
     * The status of this invitation
     *
     * @return invite status
     */
    public InviteStatus getStatus() {
        return inviteStatus;
    }
}
