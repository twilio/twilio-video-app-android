package com.twilio.signal.impl;

import com.twilio.signal.impl.TrackInfo;


public class TrackInfoImpl implements TrackInfo {
	protected String participantAddress;
	protected String trackId;

	public TrackInfoImpl(String participantAddress, String trackId) {
		this.participantAddress = participantAddress;
		this.trackId = trackId;
	}	

	@Override
	public String getParticipantAddress() {
		return participantAddress;
	}

	@Override
	public String getTrackId() {
		return trackId;
	}

}
