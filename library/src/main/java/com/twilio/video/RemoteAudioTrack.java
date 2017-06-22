package com.twilio.video;

/**
 * A remote audio track represents a remote audio source.
 */
public class RemoteAudioTrack extends AudioTrack {
    private final String sid;

    RemoteAudioTrack(String trackId, boolean isEnabled, String sid) {
        super(trackId, isEnabled);
        this.sid = sid;
    }

    /**
     * Returns a string that uniquely identifies the remote audio track within the scope
     * of a {@link Room}.
     *
     * @return sid
     */
    public String getSid() {
        // TODO: Implement once proper SID is used to build object
        throw new UnsupportedOperationException();
    }

    /**
     * Enables playback of remote audio track. When playback is disabled the audio is muted.
     *
     * @param enable the desired playback state of the remote audio track.
     */
    public void enablePlayback(boolean enable) {
        // TODO: Implement once playback can be enabled
        throw new UnsupportedOperationException();
    }

    /**
     * Check if playback on the remote audio track is enabled.
     *
     * When the value is false, the remote audio track is muted. When the value is true the remote
     * audio track is playing.
     *
     * @return true if remote audio is enabled.
     */
    public boolean isPlaybackEnabled() {
        // TODO: Impelement once playback can be enabled
        throw new UnsupportedOperationException();
    }
}
