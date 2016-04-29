package com.twilio.conversations;

/**
 * ConversationsClientListener interface defines a set of callbacks for events related to a
 * {@link ConversationsClient}.
 *
 */
public interface ConversationsClientListener {
    /**
     * This method notifies the listener that the client is successfully listening for incoming
     * invitations. This method will be invoked after a successful call to
     * {@link ConversationsClient#listen()} or after a reconnect event.
     *
     * @param conversationsClient The client that is listening for incoming invitations.
     */
    void onStartListeningForInvites(ConversationsClient conversationsClient);

    /**
     * This method notifies the listener that the client is no longer listening for invitations.
     * This method will be invoked after a successful call to {@link ConversationsClient#unlisten()}
     * or when a network change occurs.
     *
     * @param conversationsClient The client that is no longer listening for incoming invitations.
     */
    void onStopListeningForInvites(ConversationsClient conversationsClient);

    /**
     *
     * This method notifies the listener that the client failed to start listening for invitations.
     * This method is invoked after an unsuccessful call to {@link ConversationsClient#listen()}.
     *
     * @param conversationsClient The conversations client that failed to start listening for
     *                            incoming invitations.
     * @param exception Exception containing information that caused the failure.
     */
    void onFailedToStartListening(ConversationsClient conversationsClient,
                                  TwilioConversationsException exception);

    /**
     * This method notifies the listener that the client has a pending invitation for a
     * conversation.
     *
     * @param incomingInvite The invitation object.
     */
    void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite);

    /**
     * This method notifies the listener that the incoming invite was cancelled.
     *
     * @param incomingInvite The invitation object.
     */
    void onIncomingInviteCancelled(ConversationsClient conversationsClient,
                                   IncomingInvite incomingInvite);
}
