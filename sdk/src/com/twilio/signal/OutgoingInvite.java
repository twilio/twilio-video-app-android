package com.twilio.signal;

import java.util.List;
import java.util.Set;

public interface OutgoingInvite extends Invite {

	/**
	 * Cancel this invitation
	 */
	public void cancel();

	/**
	 * Returns the identities of the participants being invited to the conversation 
	 * 
	 * @return list of participant identities invited to conversation 
	 */
	public Set<String> to();

}
