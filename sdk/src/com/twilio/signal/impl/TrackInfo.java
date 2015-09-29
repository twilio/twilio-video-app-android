package com.twilio.signal.impl;

import com.twilio.signal.TrackOrigin;

public interface TrackInfo {

	public String getParticipantAddress();

	public String getTrackId();

	public TrackOrigin getTrackOrigin();
}
