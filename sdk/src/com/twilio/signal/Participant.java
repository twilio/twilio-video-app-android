package com.twilio.signal;

public interface Participant {

	/** Participant identity */
	String getIdentity();

	/** This participants media */
	Media getMedia();

	void setParticipantListener(ParticipantListener participantListener);

	ParticipantListener getParticipantListener();

	String getSid();

}
