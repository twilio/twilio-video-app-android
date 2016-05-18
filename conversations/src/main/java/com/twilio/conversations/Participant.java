package com.twilio.conversations;


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
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }
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
         * a {@link VideoTrack} to this {@link Conversation}
         *
         * @param conversation The conversation associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track provided by this conversation
         */
        void onVideoTrackAdded(Conversation conversation,
                               Participant participant,
                               VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * a {@link VideoTrack} from this {@link Conversation}
         *
         * @param conversation The conversation associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track removed from this conversation
         */
        void onVideoTrackRemoved(Conversation conversation,
                                 Participant participant,
                                 VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Conversation}
         *
         * @param conversation The conversation associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track added to this conversation
         */
        void onAudioTrackAdded(Conversation conversation,
                               Participant participant,
                               AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Conversation}
         *
         * @param conversation The conversation associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track removed from this conversation
         */
        void onAudioTrackRemoved(Conversation conversation,
                                 Participant participant,
                                 AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been enabled
         *
         * @param conversation The conversation associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track enabled in this conversation
         */
        void onTrackEnabled(Conversation conversation,
                            Participant participant,
                            MediaTrack mediaTrack);

        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been disabled
         *
         * @param conversation The conversation associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track disabled in this conversation
         */
        void onTrackDisabled(Conversation conversation,
                             Participant participant,
                             MediaTrack mediaTrack);
    }
}
