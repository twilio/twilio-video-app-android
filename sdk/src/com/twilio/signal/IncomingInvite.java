package com.twilio.signal;

import java.util.Set;

public interface IncomingInvite {

	/**
	 * Accept this invitation
	 */
	void accept(LocalMedia localMedia, ConversationCallback conversationCallback);

	/**
	 * Reject this invitation
	 */
	void reject();

	/**
	 * Returns the SID of the conversation you are invited to
	 * 
	 * @return conversation SID 
	 */
	String getConversationSid();

	/**
	 * Returns the identity of the invitee
	 * 
	 * @return invitee identity
	 */
	String getInvitee();

	/**
	 * Returns the list of participants already in the conversation 
	 * 
	 * @return list of participants 
	 */
	Set<String> getParticipants();

	/**
	 * The status of this invite
	 *
	 * @return invite status
	 */
	InviteStatus getInviteStatus();
}
