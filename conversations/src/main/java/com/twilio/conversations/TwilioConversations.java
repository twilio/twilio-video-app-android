package com.twilio.conversations;

import android.content.Context;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.impl.TwilioConversationsImpl;


/**
 * Twilio Conversations SDK
 *
 * <h3>Threading model</h3>
 *
 * <p>Registered listeners are invoked on the thread used to initialize the
 * {@link TwilioConversations} with the exception of {@link LocalMediaListener}.
 * The {@link LocalMediaListener} is invoked on the thread used to create {@link LocalMedia} or
 * when {@link LocalMedia#setLocalMediaListener(LocalMediaListener)} is called.
 * If any of these threads do not provide a Looper, the SDK will attempt to use the main thread.</p>
 *
 */
public class TwilioConversations {

    private TwilioConversations() {}


}
