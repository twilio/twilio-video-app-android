package com.twilio.video;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * LocalMedia provides local audio and video track management.
 *
 * <p>LocalMedia can be published to the participants of a {@link Room} when provided via
 * {@link ConnectOptions}. All track operations will be published to participants after
 * connected.</p>
 */
public class LocalMedia {
    private static final String RELEASE_MESSAGE_TEMPLATE = "LocalMedia released %s unavailable";
    private static final Logger logger = Logger.getLogger(LocalMedia.class);

    private long nativeLocalMediaHandle;
    private final List<LocalAudioTrack> localAudioTracks = new ArrayList<>();
    private final List<LocalVideoTrack> localVideoTracks = new ArrayList<>();
    private final MediaFactory mediaFactory;

    /**
     * Creates a new local media.
     *
     * @param context application context
     * @return a new local media instance
     */
    public static LocalMedia create(Context context) {
        return MediaFactory.instance(context).createLocalMedia();
    }

    LocalMedia(long nativeLocalMediaHandle, MediaFactory mediaFactory) {
        this.nativeLocalMediaHandle = nativeLocalMediaHandle;
        this.mediaFactory = mediaFactory;
    }

    /**
     * Returns a list of all currently added audio tracks.
     */
    public List<LocalAudioTrack> getAudioTracks() {
        checkReleased("getAudioTracks");
        return localAudioTracks;
    }

    /**
     * Returns a list of all currently added video tracks.
     */
    public List<LocalVideoTrack> getVideoTracks() {
        checkReleased("getVideoTracks");
        return localVideoTracks;
    }

    /**
     * Adds audio track to local media.
     *
     * @param enabled initial state of audio track.
     * @return local audio track if successfully added or null if audio track could not be added.
     */
    public LocalAudioTrack addAudioTrack(boolean enabled) {
        checkReleased("addAudioTrack");
        LocalAudioTrack localAudioTrack = nativeAddAudioTrack(nativeLocalMediaHandle, enabled);

        if (localAudioTrack != null) {
            localAudioTracks.add(localAudioTrack);
            return localAudioTrack;
        } else {
            logger.e("Failed to create local audio track");
        }

        return localAudioTrack;
    }

    /**
     * Removes audio track from local media.
     *
     * @param localAudioTrack local audio track to be removed.
     * @return true if the removal succeeded or false if the audio track could not be removed.
     */
    public boolean removeAudioTrack(LocalAudioTrack localAudioTrack) {
        checkReleased("removeAudioTrack");
        boolean result = false;

        if (localAudioTrack != null && localAudioTracks.contains(localAudioTrack)) {
            localAudioTrack.release();
            result = nativeRemoveAudioTrack(nativeLocalMediaHandle, localAudioTrack.getTrackId());

            if (!result) {
                logger.e("Failed to remove audio track");
            } else {
                localAudioTracks.remove(localAudioTrack);
            }
        }

        return result;
    }

    /**
     * Adds local video track to local media.
     *
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @return local video track if successfully added or null if video track could not be added.
     */
    public LocalVideoTrack addVideoTrack(boolean enabled, VideoCapturer videoCapturer) {
        return addVideoTrack(enabled, videoCapturer, null);
    }

    /**
     * Adds local video track to local media.
     *
     * @param enabled initial state of video track.
     * @param videoCapturer capturer that provides video frames.
     * @param videoConstraints constraints to be applied on video track.
     * @return local video track if successfully added or null if video track could not be added.
     */
    public LocalVideoTrack addVideoTrack(boolean enabled,
                                         VideoCapturer videoCapturer,
                                         VideoConstraints videoConstraints) {
        checkReleased("addVideoTrack");
        LocalVideoTrack localVideoTrack = nativeAddVideoTrack(nativeLocalMediaHandle,
                enabled,
                videoCapturer,
                videoConstraints);

        if (localVideoTrack != null) {
            localVideoTracks.add(localVideoTrack);
            return localVideoTrack;
        } else {
            logger.e("Failed to create local video track");
        }

        return localVideoTrack;
    }

    /**
     * Removes video track from local media.
     *
     * @param localVideoTrack local video track to be removed.
     * @return true if the removal succeeded or false if the video track could not be removed.
     */
    public boolean removeVideoTrack(LocalVideoTrack localVideoTrack) {
        checkReleased("removeVideoTrack");
        boolean result = false;

        if (localVideoTrack != null && localVideoTracks.contains(localVideoTrack)) {
            localVideoTrack.release();
            result = nativeRemoveVideoTrack(nativeLocalMediaHandle, localVideoTrack.getTrackId());

            if (!result) {
                logger.e("Failed to remove video track");
            } else {
                localVideoTracks.remove(localVideoTrack);
            }
        }

        return result;
    }

    /**
     * Releases local media. This method must be called when local media is no longer needed. All
     * audio and video tracks will be removed. Local media should not be used after calling this
     * method.
     */
    public void release() {
        if (nativeLocalMediaHandle != 0) {
            while (!localAudioTracks.isEmpty()) {
                removeAudioTrack(localAudioTracks.get(0));
            }
            while (!localVideoTracks.isEmpty()) {
                removeVideoTrack(localVideoTracks.get(0));
            }
            nativeRelease(nativeLocalMediaHandle);
            nativeLocalMediaHandle = 0;

            mediaFactory.release();
        }
    }

    long getNativeLocalMediaHandle() {
        return nativeLocalMediaHandle;
    }

    private void checkReleased(String methodName) {
        if (nativeLocalMediaHandle == 0) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }

    private native LocalAudioTrack nativeAddAudioTrack(long nativeLocalMediaHandle,
                                                       boolean enabled);
    private native boolean nativeRemoveAudioTrack(long nativeLocalMediaHandle, String trackId);
    private native LocalVideoTrack nativeAddVideoTrack(long nativeLocalMediaHandle,
                                                       boolean enabled,
                                                       VideoCapturer videoCapturer,
                                                       VideoConstraints videoConstraints);
    private native boolean nativeRemoveVideoTrack(long nativeLocalMediaHandle, String trackId);
    private native void nativeRelease(long nativeLocalMediaHandle);
}