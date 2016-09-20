package com.twilio.video;

/**
 * A representation of a media track.
 */
interface Track {
    /**
     * Returns the id associated with the track.
     */
    String getTrackId();

    /**
     * Checks if the track is enabled.
     */
    boolean isEnabled();
}
