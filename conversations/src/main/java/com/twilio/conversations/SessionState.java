package com.twilio.conversations;

public enum SessionState {
	INITIALIZED,
	STARTING,
	START_FAILING,
	IN_PROGRESS,
	START_FAILED,
	STOPPING,
	STOPPED,
	STOP_FAILED
}