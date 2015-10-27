package com.twilio.signal;

import java.util.Set;

import com.twilio.signal.impl.EndpointImpl;

/**
 * An instance of Endpoint is an object that knows how to interface with Twilio SIGNAL Services.
 *
 * A Endpoint is the primary entry point for Twilio SIGNAL Client. An Android application should initialize a Endpoint
 * with a Capability Token to talk to Twilio SIGNAL services.
 *
 * @see EndpointListener
 */


public interface Endpoint {
	
	/**
	 * Key into an Intent's extras data that points to a {@link EndpointImpl} object.
	 */
	public static final String EXTRA_DEVICE = "com.twilio.signal.EndpointImpl";
	
	/**
	 * Sets a new {@link EndpointListener} object to respond to device events.
	 * 
	 * @param listener A {@link EndpointListener}, or null
	 */
	public void setEndpointListener(EndpointListener listener);
	
	/**
	 * Gets address of this endpoint on the network for incoming calls
	 * 
	 * @return address of this endpoint
 	 */
	public String getAddress();
	
	/**
	 * Reflects current listening state of the endpoint
	 * 
	 * @return @return <code>true</code> if endpoint is listening, </code>false</code> otherwise.
 	 */
	public boolean isListening();
	
	/**
	 * Start listening for incoming Conversation.
	 * 
	 * Registers this Endpoint with a given token. Endpoint is ready to receive Conversation invite once it registers.
	 *
	 */
	public void listen();
	
	/**
	 * 
	 * Stop listening for incoming Conversations.
	 * 
	 * Unregisters this Endpoint. Once this method is called Endpoint will not be able to receive any Conversation invite
	 * until Endpoint registers again. {@link EndpointListener#onStopListening(Endpoint)} is called after unregistration completes.
	 */
	public void unlisten();
	
	/**
	 * Create conversation object which represents outgoing call
	 * 
	 * @param participants Set of participant names as Strings
	 * @param localMedia Local Media you would like to use when setting up the new conversation
	 * @param listener for Conversation events
	 */
	
	public Conversation createConversation(Set<String> participants, LocalMediaImpl localMediaImpl, ConversationListener listener);
	
	/**
	 * Free native object associated with this Endpoint
	 * 
	 * This will mark Endpoint as disposed and will throw exception in future if any method is called.
	 * After this call you should loose any reference to this object and let it be garbage collected.
	 */
	public void dispose();

}
