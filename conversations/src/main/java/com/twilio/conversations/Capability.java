package com.twilio.conversations;

import com.twilio.conversations.TwilioConversationsClient;

/**
 * This enum is used as keys to the TwilioConversationsClient's capabilities map.
 *
 * The class of the value in the Map is annotated with each enum value. 
 *
 * @see TwilioConversationsClient#getCapabilities()
 */
public enum Capability {
    /** <code>long</code> that represents the time the TwilioConversationsClient's capability token expires (number of seconds relative to the UNIX epoch). */
    EXPIRATION,
    /** <code>String</code> representing the account SID. */
    ACCOUNT_SID,
}
