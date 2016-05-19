package com.twilio.conversations;

import java.util.List;

/**
 * Provides local video and audio tracks associated with a {@link Participant}
 *
 */
public interface LocalMedia {
    interface Listener {
        /**
         * This method notifies the listener when a {@link LocalVideoTrack} has been added
         * to the {@link LocalMedia}
         *
         * @param localMedia The local media associated with this track.
         * @param videoTrack The local video track that was added to the conversation.
         */
        void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack);

        /**
         * This method notifies the listener when a {@link LocalVideoTrack} has been removed
         * from the {@link LocalMedia}
         *
         * @param localMedia The local media associated with this track.
         * @param videoTrack The local video track that was removed from the conversation.
         */
        void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack);

        /**
         * This method notifies the listener when an error occurred when
         * attempting to add or remove a {@link LocalVideoTrack}
         * @param localMedia The {@link LocalMedia} associated with the {@link LocalVideoTrack}
         * @param track The {@link LocalVideoTrack} that was requested to be added or removed to
         *              the {@link LocalMedia}
         * @param exception Provides the error that occurred while attempting to add or remove
         *                  this {@link LocalVideoTrack}. Adding or removing a local video track
         *                  can result in {@link TwilioConversationsClient#TOO_MANY_TRACKS},
         *                  {@link TwilioConversationsClient#TRACK_OPERATION_IN_PROGRESS},
         *                  {@link TwilioConversationsClient#INVALID_VIDEO_CAPTURER},
         *                  {@link TwilioConversationsClient#INVALID_VIDEO_TRACK_STATE},
         *                  or {@link TwilioConversationsClient#TRACK_CREATION_FAILED}.
         */
        void onLocalVideoTrackError(LocalMedia localMedia,
                                    LocalVideoTrack track,
                                    TwilioConversationsException exception);
    }

    /**
     * Returns the local video tracks
     *
     * @return list of local video tracks
     */
    List<LocalVideoTrack> getLocalVideoTracks();

    /**
     * Adds a local video track to list of tracks.
     *
     * <p>The result of this operation will propagate via {@link LocalMedia.Listener}. A
     * successful addition of the local video track will invoke
     * {@link LocalMedia.Listener#onLocalVideoTrackAdded(LocalMedia, LocalVideoTrack)}. If any
     * problems occur adding the video track then
     * {@link LocalMedia.Listener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
     * TwilioConversationsException)} will be invoked.</p>
     *
     * @param localVideoTrack The local video track to be added.
     */
    void addLocalVideoTrack(LocalVideoTrack localVideoTrack);

    /**
     * Removes the local video track from list of tracks.
     *
     * <p>The result of this operation will propagate via {@link LocalMedia.Listener}. A
     * successful removal of the local video track will invoke
     * {@link LocalMedia.Listener#onLocalVideoTrackRemoved(LocalMedia, LocalVideoTrack)}. If any
     * problems occur removing the video track then
     * {@link LocalMedia.Listener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
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
     * Gets the {@link Listener}
     *
     * @return media events listener
     */
    LocalMedia.Listener getLocalMediaListener();

    /**
     * Sets the {@link Listener}
     *
     * <p>The {@link Listener} is invoked on the thread that provides the
     * LocalMediaListener instance.</p>
     *
     * @param listener A media events listener
     */
    void setLocalMediaListener(LocalMedia.Listener listener);
}