package com.twilio.conversations;

public interface Participant {
    /** Participant identity */
    String getIdentity();

    /** This participants media */
    Media getMedia();

    void setParticipantListener(ParticipantListener participantListener);

    ParticipantListener getParticipantListener();

    String getSid();
}
