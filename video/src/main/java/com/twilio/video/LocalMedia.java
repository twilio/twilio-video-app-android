package com.twilio.video;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class LocalMedia {
    private static final String RELEASE_MESSAGE_TEMPLATE = "LocalMedia released %s unavailable";
    private static final Logger logger = Logger.getLogger(LocalMedia.class);

    private long nativeLocalMediaHandle;
    private final List<LocalAudioTrack> localAudioTracks = new ArrayList<>();
    private final List<LocalVideoTrack> localVideoTracks = new ArrayList<>();
    private final MediaFactory mediaFactory;

    public static LocalMedia create(Context context) {
        return MediaFactory.instance(context).createLocalMedia();
    }

    LocalMedia(long nativeLocalMediaHandle, MediaFactory mediaFactory) {
        this.nativeLocalMediaHandle = nativeLocalMediaHandle;
        this.mediaFactory = mediaFactory;
    }

    public List<LocalAudioTrack> getLocalAudioTracks() {
        checkReleased("getLocalAudioTracks");
        return localAudioTracks;
    }

    public List<LocalVideoTrack> getLocalVideoTracks() {
        checkReleased("getLocalVideoTracks");
        return localVideoTracks;
    }

    public LocalAudioTrack addAudioTrack(boolean enabled) {
        return addAudioTrack(enabled, nativeGetDefaultAudioOptions());
    }

    public LocalAudioTrack addAudioTrack(boolean enabled, AudioOptions audioOptions) {
        checkReleased("addAudioTrack");
        LocalAudioTrack localAudioTrack = nativeAddAudioTrack(nativeLocalMediaHandle,
                enabled, audioOptions);

        if (localAudioTrack != null) {
            localAudioTracks.add(localAudioTrack);
            return localAudioTrack;
        } else {
            logger.e("Failed to create local audio track");
        }

        return localAudioTrack;
    }

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

    public LocalVideoTrack addVideoTrack(boolean enabled, VideoCapturer videoCapturer) {
        return addVideoTrack(enabled, videoCapturer, null);
    }

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

    private static native AudioOptions nativeGetDefaultAudioOptions();
    private native LocalAudioTrack nativeAddAudioTrack(long nativeLocalMediaHandle,
                                                       boolean enabled,
                                                       AudioOptions audioOptions);
    private native boolean nativeRemoveAudioTrack(long nativeLocalMediaHandle, String trackId);
    private native LocalVideoTrack nativeAddVideoTrack(long nativeLocalMediaHandle,
                                                       boolean enabled,
                                                       VideoCapturer videoCapturer,
                                                       VideoConstraints videoConstraints);
    private native boolean nativeRemoveVideoTrack(long nativeLocalMediaHandle, String trackId);
    private native void nativeRelease(long nativeLocalMediaHandle);
}