package com.twilio.conversations;

/**
 * A media track can either be an {@link VideoTrack} or {@link AudioTrack}
 *
 */
public interface MediaTrack {

    /**
     * The track id associated with this media track
     *
     * @return media track id
     */
    public String getTrackId();
}
