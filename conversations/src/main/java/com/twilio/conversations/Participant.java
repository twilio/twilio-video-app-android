package com.twilio.conversations;

/**
 * A Participant represents partaker in {@link Conversation}
 */
public interface Participant {
    /** Participant identity
     *
     * @return this participant identity
     */
    String getIdentity();

    /** This participants media
     *
     * @return this participant media
     */
    Media getMedia();

    /**
     * Sets the {@link ParticipantListener} for this participant events
     * @param participantListener
     */
    void setParticipantListener(ParticipantListener participantListener);

    /**
     * Gets the {@link ParticipantListener} of this participant
     *
     * @return listener of this participant events
     */
    ParticipantListener getParticipantListener();

    /**
     * Gets SID of this participant
     * @return sid od this participant
     */
    String getSid();
}
