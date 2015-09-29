package com.twilio.signal.impl;

import com.twilio.signal.impl.TrackInfo;
import com.twilio.signal.TrackOrigin;


public class TrackInfoImpl implements TrackInfo {
	private String participantAddress;
	private String trackId;
	private TrackOrigin trackOrigin;

	public TrackInfoImpl(String participantAddress, String trackId, TrackOrigin trackOrigin) {
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
