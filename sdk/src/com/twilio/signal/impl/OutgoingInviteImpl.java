package com.twilio.signal.impl;

import com.twilio.signal.ConversationCallback;
import com.twilio.signal.OutgoingInvite;
import com.twilio.signal.InviteStatus;
import com.twilio.signal.impl.logging.Logger;

import java.util.Set;

public class OutgoingInviteImpl implements OutgoingInvite {

	static final Logger logger = Logger.getLogger(OutgoingInviteImpl.class);

	private ConversationImpl conversation;
	private EndpointImpl endpointImpl;
	private ConversationCallback conversationCallback;
	private InviteStatus inviteStatus;

	private OutgoingInviteImpl(ConversationImpl conversation,
					EndpointImpl endpointImpl,
					ConversationCallback conversationCallback) {
		this.conversation = conversation;
		this.endpointImpl = endpointImpl;
		this.conversationCallback = conversationCallback;
		this.inviteStatus = InviteStatus.PENDING;
	}

	static OutgoingInviteImpl create(EndpointImpl endpointImpl,
								 ConversationImpl conversationImpl,
								 ConversationCallback conversationCallback) {
		if(conversationImpl == null) {
			return null;
		}
		if(endpointImpl == null) {
			return null;
		}
		if(conversationCallback == null) {
			return null;
		}
		return new OutgoingInviteImpl(conversationImpl, endpointImpl, conversationCallback);
	}

	// Update the invite status
	void updateInviteStatus(InviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}

	@Override
	public void cancel() {
		if(inviteStatus == InviteStatus.PENDING) {
			logger.i("Cancelling pending invite");
			inviteStatus = InviteStatus.CANCELLED;
			conversation.disconnect();
		} else {
			logger.w("The invite was not cancelled. Invites can only be cancelled in the pending state");
		}
	}

	@Override
	public Set<String> to() {
		return conversation.getParticipants(); 
	}

	@Override
	public InviteStatus getStatus() {
		return inviteStatus;
	}

}
