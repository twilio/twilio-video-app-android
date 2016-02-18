package com.twilio.conversations.impl;

import android.os.Handler;

import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.impl.logging.Logger;
import com.twilio.conversations.impl.util.CallbackHandler;

import java.util.Set;

public class OutgoingInviteImpl implements OutgoingInvite {
    static final Logger logger = Logger.getLogger(OutgoingInviteImpl.class);
    private final Handler handler;

    private ConversationsClientImpl conversationsClientImpl;
    private ConversationImpl conversationImpl;
    private ConversationCallback conversationCallback;
    private InviteStatus inviteStatus;

    private OutgoingInviteImpl(ConversationsClientImpl conversationsClientImpl,
                               ConversationImpl conversationImpl,
                               ConversationCallback conversationCallback) {
        this.conversationImpl = conversationImpl;
        this.conversationsClientImpl = conversationsClientImpl;
        this.conversationCallback = conversationCallback;
        this.inviteStatus = InviteStatus.PENDING;
        this.handler = CallbackHandler.create();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
    }

    static OutgoingInviteImpl create(ConversationsClientImpl conversationsClientImpl,
                                     ConversationImpl conversationImpl,
                                     ConversationCallback conversationCallback) {
        if(conversationsClientImpl == null) {
            return null;
        }
        if(conversationImpl == null) {
            return null;
        }
        if(conversationCallback == null) {
            return null;
        }
        return new OutgoingInviteImpl(conversationsClientImpl, conversationImpl, conversationCallback);
    }

    Handler getHandler() {
        return handler;
    }

    void setStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    ConversationImpl getConversation() {
        return conversationImpl;
    }

    ConversationCallback getConversationCallback() {
        return conversationCallback;
    }

    @Override
    public void cancel() {
        if(inviteStatus == InviteStatus.PENDING) {
            logger.i("Cancelling pending invite");
            inviteStatus = InviteStatus.CANCELLED;
            conversationImpl.disconnect();
        } else {
            logger.w("The invite was not cancelled. Invites can only be cancelled in the pending state");
        }
    }

    @Override
    public Set<String> getInvitedParticipants() {
        return conversationImpl.getInvitedParticipants();
    }

    @Override
    public InviteStatus getStatus() {
        return inviteStatus;
    }
}
