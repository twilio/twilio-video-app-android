package com.twilio.signal;

import com.twilio.signal.impl.Track.TrackId;

public interface ConversationListener {

	/**
	 * 
	 */
	void onConnectParticipant(Conversation conversation, Participant participant);
	
	/**
	 * 
	 */
	void onFailToConnectParticipant(Conversation conversation, Participant participant, int error, String errorMessage);
	
	
	/**
	 * 
	 */
	void onDisconnectParticipant(Conversation conversation, Participant participant);
	
	
	/**
	 * 
	 */
	void onVideoAddedForParticipant(Conversation conversation, Participant participant);
	
	/**
	 * 
	 */
	void onVideoRemovedForParticipant(Conversation conversation, Participant participant);


	/**
	 * 
	 */
	void onLocalStatusChanged(Conversation conversation, Conversation.Status status);
	
	/**
	 * 
	 */
	void onConversationEnded(Conversation conversation);
	
	/**
	 * 
	 */
	void onConversationEnded(Conversation conversation, int error, String errorMessage);

}
