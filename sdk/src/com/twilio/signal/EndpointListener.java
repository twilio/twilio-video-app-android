package com.twilio.signal;

/**
 * EndpointListener interface defines a set of callbacks for events related to a
 * {@link Endpoint}.
 * 
 */
public interface EndpointListener {

	/**
	 * This method notifies the listener that the endpoint is successfully listening for invitations.
	 * 
	 * @param endpoint The endpoint that is listening for incoming connetions.
	 *           
	 */
	public void onStartListeningForInvites(Endpoint endpoint);

	/**
	 * This method notifies the listener that the endpoint is no longer listening for invitations.
	 * 
	 * @param endpoint The endpoint that is no longer listening for incoming connections.
	 *          
	 */
	public void onStopListeningForInvites(Endpoint endpoint);

	/**
	 * 
	 * This method notifies the listener that the endpoint failed to start listening for invitations.
	 * 
	 * @param endPoint The endpoint that failed to start listening for incoming connections.
	 * @param errorCode The errorCode that caused the failure.
	 * @param errorMessage The errorMessage that caused the failure.
	 */
	public void onFailedToStartListening(Endpoint endPoint, int errorCode, String errorMessage);
		
	/**
	 * This method notifies the listener that the endpoint has a pending invitation for a conversation.
	 * 
	 * @param invite The invitation object.
	 */
	public void onReceiveConversationInvite(Endpoint endpoint, Invite invite);
	
}
