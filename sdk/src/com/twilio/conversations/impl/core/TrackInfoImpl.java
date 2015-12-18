package com.twilio.conversations.impl.core;

import com.twilio.conversations.TrackOrigin;


public class TrackInfoImpl implements TrackInfo {
	private String participantIdentity;
	private String trackId;
	private TrackOrigin trackOrigin;
	private boolean enabled;

	public TrackInfoImpl(String participantIdentity, String trackId, TrackOrigin trackOrigin, boolean enabled) {
		this.participantIdentity = participantIdentity;
		this.trackId = trackId;
		this.trackOrigin = trackOrigin;
		this.enabled = enabled;
	}

	@Override
	public String getParticipantIdentity() {
		return participantIdentity;
	}

	@Override
	public String getTrackId() {
		return trackId;
	}

	@Override
	public TrackOrigin getTrackOrigin() {
		return trackOrigin;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
