package com.twilio.video;

import java.util.List;

/**
 * Interface that represents user in a {@link Room}.
 */
public interface Participant {
    /**
     * Returns unique identifier of a participant.
     */
    String getSid();

    /**
     * Returns participant identity.
     */
    String getIdentity();

    /**
     * Returns {@link AudioTrack}s of participant.
     */
    List<AudioTrack> getAudioTracks();

    /**
     * Returns {@link VideoTrack}s of participant.
     */
    List<VideoTrack> getVideoTracks();
}
