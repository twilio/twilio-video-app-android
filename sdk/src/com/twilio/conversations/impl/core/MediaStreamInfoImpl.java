package com.twilio.conversations.impl.core;

class MediaStreamInfoImpl implements MediaStreamInfo {
	
	private long sessionId;
	private long streamId;
	private String participantAddress;
	
	public MediaStreamInfoImpl(int sessionId, int streamId, String participantAddress) {
		this.sessionId = sessionId;
		this.streamId = streamId;
		this.participantAddress = participantAddress;
	}

	@Override
	public long getSessionId() {
		return sessionId;
	}

	@Override
	public long getStreamId() {
		return streamId;
	}

	@Override
	public String getParticipantAddress() {
		return participantAddress;
	}

}
