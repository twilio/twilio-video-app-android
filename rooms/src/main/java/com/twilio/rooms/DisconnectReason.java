package com.twilio.rooms;

enum DisconnectReason {
	PARTICIPANT_TERMINATED(1),
	WILL_RECONNECT_PEER(2);

	private final int code;

	DisconnectReason(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
