package com.twilio.conversations.impl.core;

public interface MediaStreamInfo {

	public long getSessionId();
	
	public long getStreamId();
	
	public String getParticipantAddress();
}
