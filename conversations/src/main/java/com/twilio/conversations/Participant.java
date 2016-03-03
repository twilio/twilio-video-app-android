package com.twilio.conversations;


public interface Participant {

    String getIdentity();

    Media getMedia();

    void setParticipantListener(ParticipantListener participantListener);

    ParticipantListener getParticipantListener();
    
    String getSid();
}
