package com.twilio.conversations.impl;

import java.util.Set;

import android.os.Handler;

import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.IceOptions;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.impl.logging.Logger;

public class IncomingInviteImpl implements IncomingInvite {
    static final Logger logger = Logger.getLogger(IncomingInviteImpl.class);
    private final Handler handler;

    private ConversationsClientImpl conversationsClientImpl;
    private ConversationImpl conversationImpl;
    private ConversationCallback conversationCallback;
    private InviteStatus inviteStatus;

    private IncomingInviteImpl(ConversationsClientImpl conversationsClientImpl,
                               ConversationImpl conversationImpl,
                               Handler handler) {
        this.conversationImpl = conversationImpl;
        this.conversationsClientImpl = conversationsClientImpl;
        inviteStatus = InviteStatus.PENDING;
        this.handler = handler;
    }

    static IncomingInviteImpl create(ConversationsClientImpl conversationsClientImpl,
                                     ConversationImpl conversationImpl,
                                     Handler handler) {
        if(conversationsClientImpl == null) {
            return null;
        }
        if(conversationImpl == null) {
            return null;
        }
        if (handler == null) {
            return null;
        }
        return new IncomingInviteImpl(conversationsClientImpl, conversationImpl, handler);
    }

    void setStatus(InviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    Conversation getConversation() {
        return conversationImpl;
    }

    void setConversationCallback(ConversationCallback conversationCallback) {
        this.conversationCallback = conversationCallback;
    }

    ConversationCallback getConversationCallback() {
        return conversationCallback;
    }

    @Override
    public void accept(LocalMedia localMedia, final ConversationCallback conversationCallback) {
        accept(localMedia, null, conversationCallback);
    }

    @Override
    public void accept(LocalMedia localMedia, IceOptions iceOptions, final ConversationCallback conversationCallback) {
        if(localMedia == null) {
            throw new IllegalStateException("LocalMedia must not be null");
        }
        if(conversationCallback == null) {
            throw new IllegalStateException("ConversationCallback must not be null");
        }

        if (inviteStatus != InviteStatus.PENDING) {
            inviteStatus = InviteStatus.FAILED;
            throw new IllegalStateException("Invite status must be PENDING");
        }

        this.conversationCallback = conversationCallback;
        conversationImpl.setLocalMedia(localMedia);

        if (maxConversationsReached()) {
            inviteStatus = InviteStatus.FAILED;
            return;
        }

        inviteStatus = InviteStatus.ACCEPTING;
        conversationsClientImpl.accept(conversationImpl, iceOptions);
    }

    @Override
    public void reject() {
        if (inviteStatus != InviteStatus.PENDING) {
            logger.w("Rejecting invite that is no longer pending");
            return;
        }
        inviteStatus = InviteStatus.REJECTED;
        conversationsClientImpl.reject(conversationImpl);
    }

    @Override
    public String getConversationSid() {
        return conversationImpl.getSid();
    }

    @Override
    public String getInviter() {
        return conversationImpl.getInviter();
    }

    @Override
    public Set<String> getParticipants() {
        return conversationImpl.getInvitedParticipants();
    }

    @Override
    public InviteStatus getInviteStatus() {
        return inviteStatus;
    }

    private boolean maxConversationsReached() {
        if (conversationsClientImpl.getActiveConversationsCount() >=
                TwilioConstants.MAX_CONVERSATIONS) {
            final TwilioConversationsException e = new TwilioConversationsException(
                    TwilioConversations.TOO_MANY_ACTIVE_CONVERSATIONS,
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
