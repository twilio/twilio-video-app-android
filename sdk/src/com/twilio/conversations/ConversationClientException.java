package com.twilio.conversations;

public class ConversationClientException extends Exception {
	
	public enum ErrorType {
		CLIENT_DISCONNECTED
	}
	
	private ErrorType errorType;
	
	public ConversationClientException(ErrorType errorType, String errorMessage) {
		super(errorMessage);
		this.errorType = errorType;
	}
	
	public ErrorType getErrorType() {
		return errorType;
	}

}
