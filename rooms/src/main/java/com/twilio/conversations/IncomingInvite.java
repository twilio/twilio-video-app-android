package com.twilio.conversations;

import android.os.Handler;

import com.twilio.conversations.internal.Logger;

import java.util.Set;

/**
 * An IncomingInvite represents an invitation from a client to a Conversation An IncomingInvite
 * represents an invitation from a client to a {@link Conversation}
 */
public class IncomingInvite {
    static final Logger logger = Logger.getLogger(IncomingInvite.class);
    private final Handler handler;

    private TwilioConversationsClientInternal conversationsClient;
    private Conversation conversation;
    private ConversationCallback conversationCallback;
    private InviteStatus inviteStatus;

    private IncomingInvite(TwilioConversationsClientInternal conversationsClient,
                           Conversation conversation,
                           Handler handler) {
        this.conversation = conversation;
        this.conversationsClient = conversationsClient;
        inviteStatus = InviteStatus.PENDING;
        this.handler = handler;
    }

    static IncomingInvite create(TwilioConversationsClientInternal conversationsClient,
                                 Conversation conversation,
                                 Handler handler) {
        if(conversationsClient == null) {
            return null;
        }
        if(conversation == null) {
            return null;
        }
        if (handler == null) {
            return null;
        }
        return new IncomingInvite(conversationsClient, conversation, handler);
    }

    void setStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    Conversation getConversation() {
        return conversation;
    }

    void setConversationCallback(ConversationCallback conversationCallback) {
        this.conversationCallback = conversationCallback;
    }

    ConversationCallback getConversationCallback() {
        return conversationCallback;
    }

    /**
     * Accepts this invitation. {@link ConversationCallback#onConversation(Conversation,
     * TwilioConversationsException)} will be invoked when the {@link Conversation} is available.
     */
    public void accept(LocalMedia localMedia, final ConversationCallback conversationCallback) {
        accept(localMedia, null, conversationCallback);
    }

    /**
     * Accepts this invitation with custom ICE options.
     * {@link ConversationCallback#onConversation(Conversation, TwilioConversationsException)}
     * will be invoked when the {@link Conversation} is available.
     */
    public void accept(LocalMedia localMedia, IceOptions iceOptions,
                       final ConversationCallback conversationCallback) {
        if(localMedia == null) {
            throw new IllegalStateException("LocalMedia must not be null");
        }
        if(conversationCallback == null) {
            throw new IllegalStateException("ConversationCallback must not be null");
        }

        if (inviteStatus != InviteStatus.PENDING) {
            inviteStatus = InviteStatus.FAILED;
            logger.w("Invite status must be PENDING to accept");
            return;
        }

        this.conversationCallback = conversationCallback;
        conversation.setLocalMedia(localMedia);

        if (maxConversationsReached()) {
            inviteStatus = InviteStatus.FAILED;
            return;
        }

        inviteStatus = InviteStatus.ACCEPTING;
        conversationsClient.accept(conversation, iceOptions);
    }

    /**
     * Rejects this invitation
     */
    public void reject() {
        if (inviteStatus != InviteStatus.PENDING) {
            logger.w("Rejecting invite that is no longer pending");
            return;
        }
        inviteStatus = InviteStatus.REJECTED;
        conversationsClient.reject(conversation);
    }

    /**
     * Returns the SID of the conversation you are invited to
     *
     * @return conversation SID
     */
    public String getConversationSid() {
        return conversation.getSid();
    }

    /**
     * Returns the identity of the inviter
     *
     * @return inviter identity
     */
    public String getInviter() {
        return conversation.getInviter();
    }

    /**
     * Returns the list of participants already in the conversation
     *
     * @return list of participants
     */
    public Set<String> getParticipants() {
        return conversation.getInvitedParticipants();
    }

    /**
     * Gets the status of this invite
     *
     * @return invite status
     */
    public InviteStatus getInviteStatus() {
        return inviteStatus;
    }

    private boolean maxConversationsReached() {
        if (conversationsClient.getActiveConversationsCount() >=
                TwilioConstants.MAX_CONVERSATIONS) {
            final TwilioConversationsException e = new TwilioConversationsException(
                    TwilioConversationsClient.TOO_MANY_ACTIVE_CONVERSATIONS,
                    "Maximum number of active conversations has reached. " +
                            "Max conversations limit is: " + TwilioConstants.MAX_CONVERSATIONS);
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        conversationCallback.onConversation(null, e);
                    }
                });
            }
            return true;
        }
        return false;
    }
}
