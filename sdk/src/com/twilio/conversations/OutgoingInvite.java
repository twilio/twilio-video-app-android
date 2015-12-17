package com.twilio.conversations;

import java.util.List;
import java.util.Set;

public interface OutgoingInvite {

	/**
	 * Cancel this invitation
	 */
	public void cancel();

	/**
	 * Returns the identities of the participants invited to the conversation
	 * 
	 * @return list of participant identities invited to conversation 
	 */
	public Set<String> getInvitedParticipants();

	/**
	 * The status of this invitation
	 *
	 * @return invite status
	 */
	public InviteStatus getStatus();

}
