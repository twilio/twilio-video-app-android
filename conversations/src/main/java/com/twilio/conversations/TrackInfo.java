package com.twilio.conversations;

import com.twilio.conversations.TrackOrigin;

public interface TrackInfo {
    String getParticipantIdentity();

    String getTrackId();

    TrackOrigin getTrackOrigin();

    boolean isEnabled();
}
