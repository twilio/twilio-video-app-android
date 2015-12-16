package com.twilio.signal;

public interface ConversationListener {

	/**
	 * 
	 */
	void onParticipantConnected(Conversation conversation, Participant participant);
	
	/**
	 * 
	 */
	void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e);
	
	
	/**
	 * 
	 */
	void onParticipantDisconnected(Conversation conversation, Participant participant);
	

	/**
	 *
	 */
	void onConversationEnded(Conversation conversation, ConversationException e);

}
