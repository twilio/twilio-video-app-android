package com.twilio.signal.impl.core;

public interface SessionStatistics {
	
	public long getSessionId();
	
	public String getEndpointAddress();
	
	public String getParticipantAddress();
	
	//public ConnectionStatsReport getReport();

}
