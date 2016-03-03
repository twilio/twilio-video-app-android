package com.twilio.conversations;

import java.util.List;

/**
 * Provides local video and audio tracks associated with a {@link Participant}
 *
 */
public interface LocalMedia {
    /**
     * Returns the local video tracks
     *
     * @return list of local video tracks
     */
    List<LocalVideoTrack> getLocalVideoTracks();

    /**
     * Adds a local video track to list of tracks.
     *
     * @param track
     *
     * @note This operation may not always succeed. If a failure occurs the error
     * is reported in {@link LocalMediaListener}.onLocalVideoTrackError(...)
     */
    void addLocalVideoTrack(LocalVideoTrack track);

    /**
     * Removes the local video track from list of tracks.
     *
     * @param track
     *
     * @note This operation may not always succeed. If a failure occurs the error
     * is reported in {@link LocalMediaListener}.onLocalVideoTrackError(...)
     */
    void removeLocalVideoTrack(LocalVideoTrack track);

    /**
     * Specifies whether or not your local audio should be muted
     *
     * @param on <code>true</code> if local audio should be muted, false otherwise
     * @return <code>true</code> if mute operation is successful
     */
    boolean mute(boolean on);

    /**
     * Indicates whether your local audio is muted.
     *
     * @return <code>true</code> if local audio is muted, false otherwise
     */
    boolean isMuted();

    /**
     * Enables local audio to media session.
     *
     * @return true if local audio is enabled
     */
    boolean addMicrophone();

    /**
     * Disables local audio from the media session.
     *
     * @return true if local audio is disabled
     */
    boolean removeMicrophone();

    /**
     * Indicates whether or not your local
     * audio is enabled in the media session
     *
     * @return true if local audio is enabled
     */
    boolean isMicrophoneAdded();

    /**
     * Gets the {@link LocalMediaListener}
     *
     * @return media events listener
     */
    LocalMediaListener getLocalMediaListener();

    /**
     * Sets the {@link LocalMediaListener}
     *
     * @param listener A media events listener
     */
    void setLocalMediaListener(LocalMediaListener listener);
}