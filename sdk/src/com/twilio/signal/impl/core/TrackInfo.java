package com.twilio.signal.impl.core;

import com.twilio.signal.TrackOrigin;

public interface TrackInfo {

	public String getParticipantAddress();

	public String getTrackId();

	public TrackOrigin getTrackOrigin();
}
