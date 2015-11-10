package com.twilio.signal;

public class CameraException extends Exception {

	private static final long serialVersionUID = 853004373043422260L;
	
	private String errorMessage;
	
	public CameraException(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		return errorMessage;
	}
	
	

}
