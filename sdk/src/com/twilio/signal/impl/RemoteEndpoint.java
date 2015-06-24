package com.twilio.signal.impl;


public interface RemoteEndpoint {
	
	/**
	 * Address used to identify the remote endpoint.
	 */
	public String address = null;
	
	/**
	 * This is the Stream object defining media for remote endpoint.
	 */
	public Stream stream = null;
	
}
