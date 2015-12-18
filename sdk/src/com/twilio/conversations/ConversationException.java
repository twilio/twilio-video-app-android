package com.twilio.conversations;

import java.util.Locale;

/**
 * A class that provides information about a {@link Conversation} error.
 *
 */
public class ConversationException extends Exception {
	

	private static final long serialVersionUID = 8881301848720707153L;
	
	private int errorCode;
	private String errorMessage;
	private String domain;
	
	public ConversationException(String domain, int errorCode, String errorMessage) {
		this.domain = domain;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		return String.format(Locale.getDefault(),"domain:%s, code:%d, message:%s", domain, errorCode, errorMessage);
	}

}
