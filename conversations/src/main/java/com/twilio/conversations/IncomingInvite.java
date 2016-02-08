package com.twilio.conversations;

import java.util.Set;

/**
 * An IncomingInvite represents an invitation from a client to a Conversation An IncomingInvite represents an invitation from a client to a {@link Conversation}
 */
public interface IncomingInvite {

	/**
	 * Accepts this invitation
	 */
	void accept(LocalMedia localMedia, ConversationCallback conversationCallback);

	/**
	 * Rejects this invitation
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
	 * Gets the status of this invite
	 *
	 * @return invite status
	 */
	InviteStatus getInviteStatus();
}
