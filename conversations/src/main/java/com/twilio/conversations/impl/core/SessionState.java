package com.twilio.conversations.impl.core;

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