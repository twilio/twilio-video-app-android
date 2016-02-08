package com.twilio.conversations.impl.core;

public enum DisconnectReason {
	PARTICIPANT_TERMINATED(1),
	WILL_RECONNECT_PEER(2);
	
	private int code;
	
	private DisconnectReason(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
