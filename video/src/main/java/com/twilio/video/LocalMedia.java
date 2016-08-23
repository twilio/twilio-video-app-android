package com.twilio.video;

import android.content.Context;

import com.twilio.video.internal.Logger;

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
        long nativeAudioTrack = nativeAddAudioTrack(nativeLocalMediaHandle, enabled, audioOptions);
        LocalAudioTrack localAudioTrack = null;

        if (nativeAudioTrack != 0) {
            org.webrtc.AudioTrack webRtcAudioTrack = new org.webrtc.AudioTrack(nativeAudioTrack);
            localAudioTrack = new LocalAudioTrack(webRtcAudioTrack);

            localAudioTrack.enable(enabled);
            localAudioTracks.add(localAudioTrack);
            return localAudioTrack;
        } else {
            logger.e("Failed to create local audio track");
        }

        return localAudioTrack;
    }

    public boolean removeAudioTrack(LocalAudioTrack localAudioTrack) {
        checkReleased("removeAudioTrack");
        boolean result = nativeRemoveAudioTrack(nativeLocalMediaHandle,
                localAudioTrack.getTrackId());

        if (!result) {
            logger.e("Failed to remove audio track");
        } else {
            localAudioTracks.remove(localAudioTrack);
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
        long nativeVideoTrack = nativeAddVideoTrack(nativeLocalMediaHandle,
                enabled,
                new VideoCapturerDelegate(videoCapturer),
                videoConstraints);
        LocalVideoTrack localVideoTrack = null;

        if (nativeVideoTrack != 0) {
            org.webrtc.VideoTrack webRtcVideoTrack = new org.webrtc.VideoTrack(nativeVideoTrack);
            localVideoTrack = new LocalVideoTrack(webRtcVideoTrack, videoCapturer, null);

            localVideoTrack.enable(enabled);
            localVideoTracks.add(localVideoTrack);
            return localVideoTrack;
        } else {
            logger.e("Failed to create local audio track");
        }

        return localVideoTrack;
    }

    public boolean removeLocalVideoTrack(LocalVideoTrack localVideoTrack) {
        checkReleased("removeVideoTrack");

        boolean result = nativeRemoveVideoTrack(nativeLocalMediaHandle,
                localVideoTrack.getTrackId());

        if (!result) {
            logger.e("Failed to remove video track");
        } else {
            localVideoTracks.remove(localVideoTrack);
        }

        return result;
    }

    public void release() {
        if (nativeLocalMediaHandle != 0) {
            nativeRelease(nativeLocalMediaHandle);
            nativeLocalMediaHandle = 0;
            localAudioTracks.clear();
            localVideoTracks.clear();
            mediaFactory.release();
        }
    }

    private void checkReleased(String methodName) {
        if (nativeLocalMediaHandle == 0) {
            String releaseErrorMessage = String.format(RELEASE_MESSAGE_TEMPLATE, methodName);

            throw new IllegalStateException(releaseErrorMessage);
        }
    }

    private static native AudioOptions nativeGetDefaultAudioOptions();
    private native long nativeAddAudioTrack(long nativeLocalMediaHandle,
                                            boolean enabled,
                                            AudioOptions audioOptions);
    private native boolean nativeRemoveAudioTrack(long nativeLocalMediaHandle, String trackId);
    private native long nativeAddVideoTrack(long nativeLocalMediaHandle,
                                            boolean enabled,
                                            VideoCapturerDelegate videoCapturerDelegate,
                                            VideoConstraints videoConstraints);
    private native boolean nativeRemoveVideoTrack(long nativeLocalMediaHandle, String trackId);
    private native void nativeRelease(long nativeLocalMediaHandle);
}