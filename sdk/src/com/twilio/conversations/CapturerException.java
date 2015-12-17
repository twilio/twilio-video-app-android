package com.twilio.conversations;

public class CapturerException extends Exception {

	private static final long serialVersionUID = 853004373043422260L;
	
	public static enum ExceptionDomain {
		CAPTURER,
		CAMERA
	}
	
	private String errorMessage;
	
	public CapturerException(ExceptionDomain domain, String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		return errorMessage;
	}
	
	

}
