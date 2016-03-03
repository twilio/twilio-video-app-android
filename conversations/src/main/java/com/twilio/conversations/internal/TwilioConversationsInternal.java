package com.twilio.conversations.internal;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.impl.TwilioConversationsImpl;

import java.util.Map;

/*
 * This class is used only for internal tests and purposes.
 */
public class TwilioConversationsInternal {

    /*
     * Creates a new ConversationsClient.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling token lifetime
     * @param options Map of options that override the default options.
     * Currently only one key is supported: ice_servers
     * @param listener A listener that receive events from the ConversationsClient.
     *
     * @return the initialized {@link ConversationsClient}, or null if the Twilio Conversations Client
     *         was not initialized
     */
    public static ConversationsClient createConversationsClient(TwilioAccessManager accessManager,
                                                                 Map<String, String> options,
                                                                 ConversationsClientListener listener) {
        if (accessManager == null) {
            throw new NullPointerException("access manager must not be null");
        }
        if (options == null) {
            throw new NullPointerException("options must not be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        TwilioConversationsImpl conversationsSdk = TwilioConversationsImpl.getInstance();

        if (!conversationsSdk.isInitialized()) {
            throw new IllegalStateException("Cannot create client before initialize is called");
        }

        return conversationsSdk.createConversationsClient(accessManager, options, listener);
    }
}
