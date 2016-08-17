package com.twilio.video;

import com.twilio.video.internal.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides local video and audio tracks associated with a {@link Participant}
 */
public class LocalMedia {
    private static final Logger logger = Logger.getLogger(LocalMedia.class);

    private List<LocalVideoTrack> videoTracks = new ArrayList<>();
    private boolean audioEnabled;
    private boolean audioMuted;
    private LocalMedia.Listener localMediaListener;

    public LocalMedia(LocalMedia.Listener localMediaListener) {
        this.localMediaListener = localMediaListener;
        this.audioEnabled = true;
        this.audioMuted = false;
    }

    /**
     * Gets the {@link Listener}
     *
     * @return media events listener
     */
    public LocalMedia.Listener getLocalMediaListener() {
        return localMediaListener;
    }

    /**
     * Sets the {@link Listener}
     *
     * <p>The {@link Listener} is invoked on the thread that provides the
     * LocalMediaListener instance.</p>
     *
     * @param listener A media events listener
     */
    public void setLocalMediaListener(LocalMedia.Listener listener) {
        localMediaListener = listener;
    }

    /**
     * Specifies whether or not your local audio should be muted
     *
     * @param on <code>true</code> if local audio should be muted, false otherwise
     * @return <code>true</code> if mute operation is successful
     */
    public boolean mute(boolean on) {
        // TODO: impelement me
        return false;
    }

    /**
     * Indicates whether your local audio is muted.
     *
     * @return <code>true</code> if local audio is muted, false otherwise
     */
    public boolean isMuted() {
        return audioMuted;
    }

    /**
     * Returns the local video tracks
     *
     * @return list of local video tracks
     */
    public List<LocalVideoTrack> getLocalVideoTracks() {
        return new ArrayList<>(videoTracks);
    }

    /**
     * Adds a local video track to list of tracks.
     *
     * <p>The result of this operation will propagate via {@link LocalMedia.Listener}. A
     * successful addition of the local video track will invoke
     * {@link LocalMedia.Listener#onLocalVideoTrackAdded(LocalMedia, LocalVideoTrack)}. If any
     * problems occur adding the video track then
     * {@link LocalMedia.Listener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
     * VideoException)} will be invoked.</p>
     *
     * @param localVideoTrack The local video track to be added.
     */
    public void addLocalVideoTrack(final LocalVideoTrack localVideoTrack)
            throws IllegalArgumentException, UnsupportedOperationException {
        // TODO: implement me
    }

    /**
     * Removes the local video track from list of tracks.
     *
     * <p>The result of this operation will propagate via {@link LocalMedia.Listener}. A
     * successful removal of the local video track will invoke
     * {@link LocalMedia.Listener#onLocalVideoTrackRemoved(LocalMedia, LocalVideoTrack)}. If any
     * problems occur removing the video track then
     * {@link LocalMedia.Listener#onLocalVideoTrackError(LocalMedia, LocalVideoTrack,
     * VideoException)} will be invoked.</p>
     *
     * @param localVideoTrack The local video track to be removed
     */
    public void removeLocalVideoTrack(LocalVideoTrack localVideoTrack) throws IllegalArgumentException {
        if (videoTracks.size() == 0) {
            logger.w("There are no local video tracks in the list");
            return;
        } else if (!videoTracks.contains(localVideoTrack)) {
            logger.w("The specified local video track was not found");
            return;
        }
        // TODO: implement me
    }

    /**
     * Enables local audio to media session. {@link android.Manifest.permission#RECORD_AUDIO}
     * permission must be granted prior to invoking
     *
     * @return true if local audio is enabled
     */
    public boolean addMicrophone() {
        if (!audioEnabled) {
            audioEnabled = enableAudio(true);
            return audioEnabled;
        } else {
            return false;
        }
    }

    /**
     * Disables local audio from the media session.
     *
     * @return true if local audio is disabled
     */
    public boolean removeMicrophone() {
        if (audioEnabled) {
            audioEnabled = !enableAudio(false);
            return !audioEnabled;
        } else {
            return false;
        }
    }

    /**
     * Indicates whether or not your local
     * audio is enabled in the media session
     *
     * @return true if local audio is enabled
     */
    public boolean isMicrophoneAdded() {
        return audioEnabled;
    }

    public void release() {
        // TODO
    }

    private boolean enableAudio(boolean enable) {
        // TODO: implement me
        return false;
    }

    public interface Listener {
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
         *                  can result in {@link VideoClient#TOO_MANY_TRACKS},
         *                  {@link VideoClient#TRACK_OPERATION_IN_PROGRESS},
         *                  {@link VideoClient#INVALID_VIDEO_CAPTURER},
         *                  {@link VideoClient#INVALID_VIDEO_TRACK_STATE},
         *                  or {@link VideoClient#TRACK_CREATION_FAILED}.
         */
        void onLocalVideoTrackError(LocalMedia localMedia,
                                    LocalVideoTrack track,
                                    VideoException exception);
    }
}