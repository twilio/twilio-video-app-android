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
     * Specifies whether or not media track should be enabled
     *
     * @param enabled <code>true</code> if media track should be enabled, false otherwise
     * @return true if the operation succeeded. false if there is an operation in progress.
     */
    boolean enable(boolean enabled);

    /**
     * Returns whether or not media track is enabled
     *
     * @return <code>true</code> if media track is enabled, false otherwise
     */
    boolean isEnabled();
}
