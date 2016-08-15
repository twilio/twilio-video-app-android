package com.twilio.video;


import android.os.Handler;

public class Participant {
    private String identity;
    private String sid;
    private Media media;
    private long nativeContext;

    Participant(String identity, String sid,
                long nativeMediaContext, long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.media = new Media();
        this.nativeContext = nativeParticipantContext;
    }

    public String getIdentity() {
        return identity;
    }

    public Media getMedia() {
        return media;
    }

    public String getSid() {
        return sid;
    }

    public boolean isConnected() {
        return nativeIsConnected(nativeContext);
    }

    void setSid(String sid) {
        this.sid = sid;
    }

    void release(){
        if (nativeContext != 0) {
            nativeRelease(nativeContext);
            nativeContext = 0;
        }
    }

    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);

    // TODO: Move this listener to Media.Listener

    public interface Listener {
        /**
         * This method notifies the listener that a {@link Participant} has added
         * a {@link VideoTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track provided by this room
         */
        void onVideoTrackAdded(Room room,
                               Participant participant,
                               VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * a {@link VideoTrack} from this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track removed from this room
         */
        void onVideoTrackRemoved(Room room,
                                 Participant participant,
                                 VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track added to this room
         */
        void onAudioTrackAdded(Room room,
                               Participant participant,
                               AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track removed from this room
         */
        void onAudioTrackRemoved(Room room,
                                 Participant participant,
                                 AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been enabled
         *
         * @param room The room associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track enabled in this room
         */
        void onTrackEnabled(Room room,
                            Participant participant,
                            MediaTrack mediaTrack);

        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been disabled
         *
         * @param room The room associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track disabled in this room
         */
        void onTrackDisabled(Room room,
                             Participant participant,
                             MediaTrack mediaTrack);
    }
}
