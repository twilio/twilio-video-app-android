package com.twilio.conversations;

enum SessionState {
	INITIALIZED,
	STARTING,
	START_FAILING,
	IN_PROGRESS,
	START_FAILED,
	STOPPING,
	STOPPED,
	STOP_FAILED
}