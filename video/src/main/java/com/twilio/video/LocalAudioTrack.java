package com.twilio.video;

public class LocalAudioTrack extends AudioTrack  {
    private static final Logger logger = Logger.getLogger(LocalAudioTrack.class);

    private long nativeLocalAudioTrackHandle;

    LocalAudioTrack(long nativeLocalAudioTrackHandle, String trackId, boolean enabled) {
        super(trackId, enabled);
        this.nativeLocalAudioTrackHandle = nativeLocalAudioTrackHandle;
    }

    @Override
    public synchronized boolean isEnabled() {
        if (!isReleased()) {
            return nativeIsEnabled(nativeLocalAudioTrackHandle);
        } else {
            logger.e("Local audio track is not enabled because it has been removed");

            return false;
        }
    }

    public synchronized void enable(boolean enable) {
        if (!isReleased()) {
            nativeEnable(nativeLocalAudioTrackHandle, enable);
        } else {
            logger.e("Cannot enable a local audio track that has been removed");
        }
    }

    synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeLocalAudioTrackHandle);
            nativeLocalAudioTrackHandle = 0;
        }
    }

    boolean isReleased() {
        return nativeLocalAudioTrackHandle == 0;
    }

    private native boolean nativeIsEnabled(long nativeLocalAudioTrackHandle);
    private native void nativeEnable(long nativeLocalAudioTrackHandle, boolean enable);
    private native void nativeRelease(long nativeLocalAudioTrackHandle);
}
