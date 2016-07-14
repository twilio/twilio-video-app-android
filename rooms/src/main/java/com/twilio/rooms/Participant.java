package com.twilio.rooms;


import android.os.Handler;

public class Participant {
    private String identity;
    private String sid;
    private Media media;
    private Participant.Listener participantListener;
    private Handler handler;

    public Participant(String identity, String sid) {
        this.identity = identity;
        this.sid = sid;
        this.media = new Media();
    }

    public String getIdentity() {
        return identity;
    }

    public Media getMedia() {
        return media;
    }

    public void setParticipantListener(Participant.Listener participantListener) {
        this.handler = Util.createCallbackHandler();
        this.participantListener = participantListener;
    }

    public Participant.Listener getParticipantListener() {
        return participantListener;
    }

    public String getSid() {
        return sid;
    }

    void setSid(String sid) {
        this.sid = sid;
    }

    Handler getHandler() {
        return handler;
    }

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
