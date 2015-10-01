package com.twilio.signal.impl;

public class CoreErrorImpl implements CoreError {
	
	private String errorDomain;
	private int errorCode;
	private String errorMesage;
	
	public CoreErrorImpl(String errorDomain, int errorCode, String errorMessage) {
		this.errorDomain = errorDomain;
		this.errorCode = errorCode;
		this.errorMesage = errorMessage;
	}

	@Override
	public int getCode() {
		return errorCode;
	}

	@Override
	public String getDomain() {
		return errorDomain;
	}

	@Override
	public String getMessage() {
		return errorMesage;
	}

}
