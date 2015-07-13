package com.twilio.signal;

public interface Participant {
	
	/** Callee address */
	public String getAddress();
	
	/** Reference to the Conversation this participants is in */
	public Conversation getConversation();
	
	/** Reference to Media for this participant */
	
	public Media getMedia();

}
