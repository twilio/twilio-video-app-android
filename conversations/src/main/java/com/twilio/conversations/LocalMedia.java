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
     * <p>The result of this operation will propagate via {@link LocalMediaListener}. A
     * successful addition of the local video track will invoke
     * {@link LocalMediaListener#onLocalVideoTrackAdded(LocalMedia, LocalVideoTrack)}. If any
     * problems occur adding the video track then
     * {@link LocalMediaListener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
     * TwilioConversationsException)} will be invoked.</p>
     *
     * @param localVideoTrack The local video track to be added.
     */
    void addLocalVideoTrack(LocalVideoTrack localVideoTrack);

    /**
     * Removes the local video track from list of tracks.
     *
     * <p>The result of this operation will propagate via {@link LocalMediaListener}. A
     * successful removal of the local video track will invoke
     * {@link LocalMediaListener#onLocalVideoTrackRemoved(LocalMedia, LocalVideoTrack)}. If any
     * problems occur removing the video track then
     * {@link LocalMediaListener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
     * TwilioConversationsException)} will be invoked.</p>
     *
     * @param localVideoTrack The local video track to be removed
     */
    void removeLocalVideoTrack(LocalVideoTrack localVideoTrack);

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
     * <p>The {@link LocalMediaListener} is invoked on the thread that provides the
     * LocalMediaListener instance.</p>
     *
     * @param listener A media events listener
     */
    void setLocalMediaListener(LocalMediaListener listener);
}