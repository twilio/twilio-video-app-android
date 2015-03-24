package com.twilio.signal;



/** 
 * This enum is used as keys to the Endpoint's capabilities map.
 * 
 * The class of the value in the Map is annotated with each enum value. 
 * 
 * @see Endpoint#getCapabilities()
 */
public enum Capability
{
	/** <code>long</code> that represents the time the Endpoint's capability token expires (number of seconds relative to the UNIX epoch). */
	EXPIRATION,
	/** <code>String</code> representing the account SID. */
	ACCOUNT_SID, 
};
