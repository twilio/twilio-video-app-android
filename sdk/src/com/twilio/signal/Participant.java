package com.twilio.signal;

public interface Participant {
	
	/** Callee address */
	public String getAddress();
	
	/** Reference to Media for this participant */
	public Media getMedia();

}
