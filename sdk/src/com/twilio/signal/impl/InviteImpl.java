package com.twilio.signal.impl;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.ConversationsClient;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMedia;

public class InviteImpl implements Invite {
	
	private ConversationImpl conversation;
	private ConversationsClientImpl conversationsClient;
	private String fromAddr;
	
	private InviteImpl(ConversationImpl conversation,
					ConversationsClientImpl conversationsClient,
					String[] participants) {
		this.conversation = conversation;
		this.conversationsClient = conversationsClient;
		if (participants.length > 0) {
			fromAddr = participants[0];
		}
	}
	
	public static Invite create(ConversationImpl conversation,
					ConversationsClientImpl conversationsClient,
					String[] participants) {
		if (conversation == null) {
			return null;
		}
		if (conversationsClient == null) {
			return null;
		}
		if (participants.length == 0) {
			return null;
		}
		return new InviteImpl(conversation, conversationsClient, participants);
	}

	@Override
	public String from() {
		return fromAddr;
	}

	@Override
	public ConversationsClient to() {
		return conversationsClient;
	}

	@Override
	public void reject() {
		if (conversation != null) {
			conversationsClient.reject(conversation);
			conversation = null;
		}
	}

	@Override
	public Conversation accept(LocalMedia localMedia,
			ConversationListener listener) {
		conversation.setLocalMedia(localMedia);
		conversation.setConversationListener(listener);
		conversation.start();
		return conversation;
	}

}
