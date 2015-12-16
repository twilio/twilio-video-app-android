package com.twilio.signal;

import java.util.List;

public interface IncomingInvite extends Invite {

	/**
	 * Accept this invitation
	 */
	public void accept(LocalMedia localMedia, ConversationCallback conversationCallback);

	/**
	 * Reject this invitation
	 */
	public void reject();

	/**
	 * Returns the SID of the conversation you are invited to
	 * 
	 * @return conversation SID 
	 */
	public String getConversationSid();
	
	/**
	 * Returns the identity of the invitee
	 * 
	 * @return invitee identity
	 */
	public String from();
	
	/**
	 * Returns the list of participants already in the conversation 
	 * 
	 * @return list of participants 
	 */
	public List<String> getParticipants();

}
