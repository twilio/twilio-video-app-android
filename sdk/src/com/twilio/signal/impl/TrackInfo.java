package com.twilio.signal.impl;

public interface TrackInfo {

	public enum TrackOrigin {
		LOCAL,
		REMOTE	
	}

	public String getParticipantAddress();

	public String getTrackId();

	public TrackOrigin getTrackOrigin();
}
