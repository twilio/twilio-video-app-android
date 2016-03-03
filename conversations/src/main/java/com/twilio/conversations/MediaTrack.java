package com.twilio.conversations;

/**
 * A media track can either be a {@link VideoTrack} or {@link AudioTrack}
 *
 */
public interface MediaTrack {
    /**
     * The track id associated with this media track
     *
     * @return media track id
     */
    String getTrackId();

    /**
     * The state associated with this media track
     *
     * @return state of media track
     */
    MediaTrackState getState();


    /**
     * Returns whether or not media track is enabled
     *
     * @return <code>true</code> if media track is enabled, false otherwise
     */
    boolean isEnabled();
}
