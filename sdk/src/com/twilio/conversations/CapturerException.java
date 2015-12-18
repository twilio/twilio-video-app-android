package com.twilio.conversations;

/**
 * A class that provides information about a {@link CameraCapturer} error.
 *
 */
public class CapturerException extends Exception {

	private static final long serialVersionUID = 853004373043422260L;

	private final ExceptionDomain domain;

	public static enum ExceptionDomain {
		CAPTURER,
		CAMERA
	}

	private String errorMessage;

	public CapturerException(ExceptionDomain domain, String errorMessage) {
		this.domain = domain;
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		return domain + ": " + errorMessage;
	}

}
