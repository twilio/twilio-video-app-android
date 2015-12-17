package com.twilio.signal;

public interface ConversationListener {

	/**
	 * Called when participant is connected to conversation.
	 * 
	 * @param conversation The conversation.
	 * @param participant The participant.
	 */
	void onParticipantConnected(Conversation conversation, Participant participant);
	
	/**
	 * Called when participant is not connected due to an error.
	 * 
	 * @param conversation The conversation.
	 * @param participant The participant.
	 * @param e Exception encountered in adding participant to conversation.
	 */
	void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e);
	
	
	/**
	 * Called when specified participant is disconnected from conversation either by request or due to an error.
	 * 
	 * @param conversation The conversation.
	 * @param participant The participant.
	 */
	void onParticipantDisconnected(Conversation conversation, Participant participant);
	

	/**
	 * Called when the conversation ends after the last participant leaves.
	 * 
	 * @param conversation The conversation
	 * @param e Exception (if any) encountered when conversation ends.
	 */
	void onConversationEnded(Conversation conversation, ConversationException e);

}
