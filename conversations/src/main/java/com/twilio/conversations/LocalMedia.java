package com.twilio.conversations;

import android.os.Handler;

import com.twilio.conversations.internal.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides local video and audio tracks associated with a {@link Participant}
 *
 */
public class LocalMedia {
    private List<LocalVideoTrack> videoTracks = new ArrayList<>();
    private WeakReference<Conversation> convWeak;
    private boolean audioEnabled;
    private boolean audioMuted;
    private Handler handler;
    private LocalMedia.Listener localMediaListener;

    private static int MAX_LOCAL_VIDEO_TRACKS = 1;

    private static String TAG = "LocalMedial";
    static final Logger logger = Logger.getLogger(LocalMedia.class);

    /**
     * Creates a new instance of the {@link LocalMedia}
     *
     * <p>The {@link LocalMedia.Listener} is invoked on the thread that provides the
     * LocalMediaListener instance.</p>
     *
     * @return instance of local media
     */
    public static LocalMedia create(LocalMedia.Listener localMediaListener) {
        return new LocalMedia(localMediaListener);
    }

    public LocalMedia(LocalMedia.Listener localMediaListener) {
        this.localMediaListener = localMediaListener;
        audioEnabled = true;
        audioMuted = false;

        handler = Util.createCallbackHandler();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
    }

    Handler getHandler() {
        return handler;
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
        if (convWeak != null && convWeak.get() != null && convWeak.get().getNativeHandle() != 0) {
            audioMuted = on;
            return convWeak.get().mute(on);
        } else if (audioMuted != on){
            audioMuted = on;
            return true;
        }
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
        return new ArrayList<LocalVideoTrack>(videoTracks);
    }

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
    public void addLocalVideoTrack(final LocalVideoTrack localVideoTrack)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (localVideoTrack == null) {
            throw new NullPointerException("LocalVideoTrack can't be null");
        }
        if(!localVideoTrack.getState().equals(MediaTrackState.IDLE)) {
            postVideoTrackException(localVideoTrack,
                    new TwilioConversationsException(
                            TwilioConversationsClient.TRACK_OPERATION_IN_PROGRESS,
                            " A track operation is already in progress."));
        }
        if (videoTracks.size() >= MAX_LOCAL_VIDEO_TRACKS) {
            postVideoTrackException(localVideoTrack,
                    new TwilioConversationsException(TwilioConversationsClient.TOO_MANY_TRACKS,
                            "Unable to add the local video track. Only " + MAX_LOCAL_VIDEO_TRACKS +
                                    " local video track is supported."));
        }
        if (localVideoTrack.getCameraCapturer() == null) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (localMediaListener != null) {
                            localMediaListener.onLocalVideoTrackError(
                                    LocalMedia.this,
                                    localVideoTrack,
                                    new TwilioConversationsException(
                                            TwilioConversationsClient.INVALID_VIDEO_CAPTURER,
                                            "The LocalVideoTrack must be associated with a "+
                                                    "camera capturer"));
                        }
                    }
                });
            }
        }
        videoTracks.add(localVideoTrack);
        if ((convWeak != null) &&  (convWeak.get() != null) ) {
            // LocalVideoTrack is added during conversation
            // TODO: we should use localVideoTrack.isEnabled() as second param here,
            // it is hard coded as false for now until we resolve issue with CameraCapturer starting in disabled mode.
            // This leaves responsibility to a user to unpause the capturer, which user doesn't have to do
            // during initial creation. This is inconsistent behavior and it should be more investigated.
            CameraCapturer cameraCapturer = localVideoTrack.getCameraCapturer();
            if(cameraCapturer.getCapturerState() !=
                    CameraCapturer.CapturerState.BROADCASTING) {
                logger.d("Create a new external capturer since the nativeVideoCapturer is no longer valid");
                convWeak.get().setupExternalCapturer();
            }
            boolean enabledVideo = convWeak.get().enableVideo(true, !localVideoTrack.isEnabled(),
                    localVideoTrack.getVideoConstraints());
            if(!enabledVideo) {
                // Remove the video track since it failed to be added
                videoTracks.remove(localVideoTrack);
                postVideoTrackException(localVideoTrack,
                        new TwilioConversationsException(
                                TwilioConversationsClient.TRACK_OPERATION_IN_PROGRESS,
                                " A track operation is already in progress."));
            } else {
                localVideoTrack.setTrackState(MediaTrackState.STARTING);
            }
        }
    }

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
    public void removeLocalVideoTrack(LocalVideoTrack localVideoTrack) throws IllegalArgumentException {
        if (videoTracks.size() == 0) {
            logger.w("There are no local video tracks in the list");
            return;
        } else if (!videoTracks.contains(localVideoTrack)) {
            logger.w("The specified local video track was not found");
            return;
        }
        if(localVideoTrack.getState().equals(MediaTrackState.ENDED)) {
            postVideoTrackException(localVideoTrack, new TwilioConversationsException(TwilioConversationsClient.INVALID_VIDEO_TRACK_STATE, "The provided video track is not in a valid state"));
        } else if(!localVideoTrack.getState().equals(MediaTrackState.STARTED)) {
            postVideoTrackException(localVideoTrack, new TwilioConversationsException(TwilioConversationsClient.TRACK_OPERATION_IN_PROGRESS, " A track operation is already in progress."));
        }
        if (convWeak == null || convWeak.get() == null) {
            logger.d("Conversation is null");
            return;
        }
        Conversation conv = convWeak.get();
        boolean enabled = conv.enableVideo(false, !localVideoTrack.isEnabled(), null);
        if(enabled) {
            ((LocalVideoTrack) localVideoTrack).setTrackState(MediaTrackState.ENDING);
        }
    }

    private void postVideoTrackException(final LocalVideoTrack localVideoTrack, final TwilioConversationsException trackException) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (localMediaListener != null) {
                        localMediaListener.onLocalVideoTrackError(
                                LocalMedia.this, localVideoTrack, trackException);
                    }
                }
            });
        }
    }

    LocalVideoTrack removeLocalVideoTrack(TrackInfo trackInfo) {
        for(LocalVideoTrack videoTrack : new ArrayList<>(videoTracks)) {
            if(trackInfo.getTrackId().equals(videoTrack.getTrackInfo().getTrackId())) {
                videoTracks.remove(videoTrack);
                return videoTrack;
            }
        }
        return null;
    }

    void setConversation(Conversation conversation) {
        this.convWeak = new WeakReference<>(conversation);
    }

    /**
     * Enables local audio to media session.
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

    private boolean enableAudio(boolean enable) {
        if (convWeak != null && convWeak.get() != null) {
            return convWeak.get().enableAudio(enable, audioMuted);
        } else {
            // The conversation is not ongoing. Always return true
            return true;
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
}