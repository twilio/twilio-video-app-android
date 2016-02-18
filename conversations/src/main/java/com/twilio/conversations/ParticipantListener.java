package com.twilio.conversations;

/**
 * ParticipantListener interface defines a set of callbacks for events related to a
 * {@link ParticipantListener}.
 *
 */
public interface ParticipantListener {
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
