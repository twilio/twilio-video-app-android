package com.twilio.signal.impl.core;

import com.twilio.signal.TrackOrigin;

public interface TrackInfo {

	String getParticipantIdentity();

	String getTrackId();

	TrackOrigin getTrackOrigin();

	boolean isEnabled();

}
