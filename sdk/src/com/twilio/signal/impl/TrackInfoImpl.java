package com.twilio.signal.impl;

import com.twilio.signal.impl.TrackInfo;
import com.twilio.signal.impl.TrackInfo.TrackOrigin;


public class TrackInfoImpl implements TrackInfo {
	protected String participantAddress;
	protected String trackId;
	protected TrackOrigin trackOrigin;

	public TrackInfoImpl(String participantAddress, String trackId, TrackOrigin origin) {
		this.participantAddress = participantAddress;
		this.trackId = trackId;
		this.trackOrigin = trackOrigin;
	}	

	@Override
	public String getParticipantAddress() {
		return participantAddress;
	}

	@Override
	public String getTrackId() {
		return trackId;
	}

	@Override
	public TrackOrigin getTrackOrigin() {
		return trackOrigin;
	}

}
