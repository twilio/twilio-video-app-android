package com.twilio.signal.impl;

import com.twilio.signal.ConversationsClient;


/**
 * This enum is used as keys to the ConversationsClient's capabilities map.
 * 
 * The class of the value in the Map is annotated with each enum value. 
 * 
 * @see ConversationsClient#getCapabilities()
 */
public enum Capability
{
	/** <code>long</code> that represents the time the ConversationsClient's capability token expires (number of seconds relative to the UNIX epoch). */
	EXPIRATION,
	/** <code>String</code> representing the account SID. */
	ACCOUNT_SID, 
};
