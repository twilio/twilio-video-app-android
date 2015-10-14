package com.twilio.signal.impl;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.Media;

public class InviteImpl implements Invite {
	
	private ConversationImpl conversation;
	private EndpointImpl toEndpoint;
	private String fromAddr;
	
	private InviteImpl(ConversationImpl conversation,
					EndpointImpl endpoint,
					String[] participants) {
		this.conversation = conversation;
		this.toEndpoint = endpoint;
		if (participants.length > 0) {
			fromAddr = participants[0];
		}
	}
	
	public static Invite create(ConversationImpl conversation,
					EndpointImpl endpoint,
					String[] participants) {
		if (conversation == null) {
			return null;
		}
		if (endpoint == null) {
			return null;
		}
		if (participants.length == 0) {
			return null;
		}
		return new InviteImpl(conversation, endpoint, participants);
	}

	@Override
	public String from() {
		return fromAddr;
	}

	@Override
	public Endpoint to() {
		return toEndpoint;
	}

	@Override
	public void reject() {
		if (conversation != null) {
			toEndpoint.reject(conversation);
			conversation = null;
		}
	}

	@Override
	public Conversation acceptWithLocalMedia(Media localMedia,
			ConversationListener listener) {
		
		//TODO - danger, danger - we should change type to local media instead of media
		conversation.setLocalMedia((LocalMediaImpl)localMedia);
		conversation.setConversationListener(listener);
		conversation.start();
		return conversation;
	}

}
