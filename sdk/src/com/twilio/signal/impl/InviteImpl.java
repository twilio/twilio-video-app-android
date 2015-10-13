package com.twilio.signal.impl;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.Invite;
import com.twilio.signal.Media;

public class InviteImpl implements Invite {
	
	private Conversation conversation;
	private Endpoint endpoint;
	private String fromAddr;
	
	private InviteImpl(Conversation conversation,
					Endpoint endpoint,
					String[] participants) {
		this.conversation = conversation;
		this.endpoint = endpoint;
		if (participants.length > 0) {
			fromAddr = participants[0];
		}
	}
	
	public static Invite create(Conversation conversation,
					Endpoint endpoint,
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
		
		return null;
	}

	@Override
	public Endpoint to() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reject() {
		// TODO Auto-generated method stub

	}

	@Override
	public Conversation acceptWithLocalMedia(Media localMedia,
			ConversationListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

}
