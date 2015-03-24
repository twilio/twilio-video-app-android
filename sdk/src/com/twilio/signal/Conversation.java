package com.twilio.signal;

import java.util.Set;

/**
 * A Conversation represents a communication from one Endpoint to one or more RemoteEndpoint. 
 * You should not call Conversation's constructor directly; instead, call Endpoint#createSession.
 *
 */

public interface Conversation {
	
	
	/**
	 * An enum describing the current state of the Conversation.
	 */
	public enum State
	{
		/** The Conversation has been initiated (outgoing). */
		CONNECTING,
		/** The conversation is connected and active. There is at least one RemoteEndpoint in the conversation.*/
		CONNECTED,
		/** The conversation has ended, either due to an explicit removal of all RemoteEndpoints command or an error. */
		DISCONNECTED
	};
	
	/**
	 * Retrieves the current state of the connection.
	 * 
	 * @return The connection state, as a {@link State} enum value
	 * 
	 * @see State
	 */
	public Conversation.State getState();
		
	/**
	 * Invite a set of remote endpoints to a conversation.
	 *
	 * @param participants A set of strings representing the names of the remote endpoints.
	 */
	public void inviteRemoteEndpoints(Set<String> remoteEndpoints);
	
	
	/**
	 * Invite one Participants to join the Session.
	 *
	 * @param remoteEndpoint -  A string representing the name of the remote endpoint.
	 *  
	 */
	public void inviteRemoteEndpoint(String remoteEndpoint);
	
	/**
	 * Returns the list of Participants in an active Session.
	 * 
	 * @return participants - list of {@link Participant} in this Session.
	 */	
	public Set<String> getParticipants();
	
	/**
	 * Get a remote endpoint’s media stream.
	 * 
	 * @param endpoint The remote endpoint whose stream we are fetching.
	 * @return
	 */
	public Stream getStreamFromEndpoint(String endpoint);

}
