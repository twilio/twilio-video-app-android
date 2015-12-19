package com.twilio.conversations.impl;

import android.os.Handler;

import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.impl.logging.Logger;
import com.twilio.conversations.impl.util.CallbackHandler;

import java.util.Set;

public class IncomingInviteImpl implements IncomingInvite {

	static final Logger logger = Logger.getLogger(IncomingInviteImpl.class);
	private final Handler handler;

	private ConversationsClientImpl conversationsClientImpl;
	private ConversationImpl conversationImpl;
	private ConversationCallback conversationCallback;
	private InviteStatus inviteStatus;

	private IncomingInviteImpl(ConversationsClientImpl conversationsClientImpl,
							   ConversationImpl conversationImpl) {
		this.conversationImpl = conversationImpl;
		this.conversationsClientImpl = conversationsClientImpl;
		this.inviteStatus = InviteStatus.PENDING;
		this.handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}
	}

	static IncomingInviteImpl create(ConversationsClientImpl conversationsClientImpl,
								 ConversationImpl conversationImpl) {
		if(conversationsClientImpl == null) {
			return null;
		}
		if(conversationImpl == null) {
			return null;
		}
		return new IncomingInviteImpl(conversationsClientImpl, conversationImpl);
	}

	Handler getHandler() {
		return handler;
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
	public void accept(LocalMedia localMedia, ConversationCallback conversationCallback) {
		if(localMedia == null) {
			throw new IllegalStateException("LocalMedia must not be null");
		}
		if(conversationCallback == null) {
			throw new IllegalStateException("ConversationCallback must not be null");
		}
		inviteStatus = InviteStatus.ACCEPTING;
		this.conversationCallback = conversationCallback;
		conversationImpl.setLocalMedia(localMedia);
		conversationsClientImpl.accept(conversationImpl);
	}

	@Override
	public void reject() {
		inviteStatus = InviteStatus.REJECTED;
		conversationsClientImpl.reject(conversationImpl);
	}

	@Override
	public String getConversationSid() {
		return conversationImpl.getConversationSid();
	}

	@Override
	public String getInvitee() {
		return conversationImpl.getInvitee();
	}

	@Override
	public Set<String> getParticipants() {
		return conversationImpl.getInvitedParticipants();
	}

	@Override
	public InviteStatus getInviteStatus() {
		return inviteStatus;
	}

	InviteStatus getStatus() {
		return inviteStatus;
	}
}
