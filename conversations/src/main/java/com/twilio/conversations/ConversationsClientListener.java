package com.twilio.conversations;

/**
 * ConversationsClientListener interface defines a set of callbacks for events related to a
 * {@link TwilioConversationsClient}.
 *
 */
public interface ConversationsClientListener {
    /**
     * This method notifies the listener that the client is successfully listening for incoming
     * invitations. This method will be invoked after a successful call to
     * {@link TwilioConversationsClient#listen()} or after a reconnect event.
     *
     * @param twilioConversationsClient The client that is listening for incoming invitations.
     */
    void onStartListeningForInvites(TwilioConversationsClient twilioConversationsClient);

    /**
     * This method notifies the listener that the client is no longer listening for invitations.
     * This method will be invoked after a successful call to {@link TwilioConversationsClient#unlisten()}
     * or when a network change occurs.
     *
     * @param twilioConversationsClient The client that is no longer listening for incoming invitations.
     */
    void onStopListeningForInvites(TwilioConversationsClient twilioConversationsClient);

    /**
     *
     * This method notifies the listener that the client failed to start listening for invitations.
     * This method is invoked after an unsuccessful call to {@link TwilioConversationsClient#listen()}.
     *
     * @param twilioConversationsClient The conversations client that failed to start listening for
     *                            incoming invitations.
     * @param exception Exception containing information that caused the failure.
     */
    void onFailedToStartListening(TwilioConversationsClient twilioConversationsClient,
                                  TwilioConversationsException exception);

    /**
     * This method notifies the listener that the client has a pending invitation for a
     * conversation.
     *
     * @param incomingInvite The invitation object.
     */
    void onIncomingInvite(TwilioConversationsClient twilioConversationsClient, IncomingInvite incomingInvite);

    /**
     * This method notifies the listener that the incoming invite was cancelled.
     *
     * @param incomingInvite The invitation object.
     */
    void onIncomingInviteCancelled(TwilioConversationsClient twilioConversationsClient,
                                   IncomingInvite incomingInvite);
}
