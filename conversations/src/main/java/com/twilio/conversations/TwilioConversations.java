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

    /**
     * Initialize the Twilio Conversations SDK.
     *
     * @param context
     *            The application context of your Android application
     *
     * @param initListener
     *            A {@link TwilioConversationsClient.InitListener} that will notify you
     *            when the service is ready
     *
     */
    public static void initialize(Context context,
                                  TwilioConversationsClient.InitListener initListener) {
        if (context == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (initListener == null) {
            throw new NullPointerException("initListener must not be null");
        }

        TwilioConversationsImpl.getInstance().initialize(context, initListener);
    }

    /**
     * Dispose the Twilio Conversations SDK. Note that once this completes
     * all {@link TwilioConversationsClient} are destroyed and are no longer usable.
     *
     */
    public static void destroy() {
        TwilioConversationsImpl.getInstance().destroy();
    }

    /**
     * Informs whether {@link TwilioConversations} is initialized or not.
     *
     * @return <code>true</code> if Twilio Conversations client is initialized, <code>false</code> otherwise.
     */
    public static boolean isInitialized() {
        return TwilioConversationsImpl.getInstance().isInitialized();
    }

    /**
     * Gets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @return the logging level
     */
    public static TwilioConversationsClient.LogLevel getLogLevel() {
        return TwilioConversationsImpl.getLogLevel();
    }

    /**
     * Sets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(TwilioConversationsClient.LogLevel level) {
        TwilioConversationsImpl.setLogLevel(level);
    }

    /**
     * Sets the logging level for messages logged by a specific module.
     *
     * @param module The module for this log level
     * @param level The logging level
     */
    public static void setModuleLogLevel(TwilioConversationsClient.LogModule module, TwilioConversationsClient.LogLevel level) {
        TwilioConversationsImpl.setModuleLogLevel(module, level);
    }

    /**
     * Creates a new {@link TwilioConversationsClient}.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling token lifetime
     * @param listener A listener that receive events from the TwilioConversationsClient.
     *
     * @return the initialized {@link TwilioConversationsClient}, or null if the Twilio Conversations Client
     *         was not initialized
     */
    public static TwilioConversationsClient createConversationsClient(TwilioAccessManager accessManager,
                                                                      ConversationsClientListener listener) {
        return createConversationsClient(accessManager, null, listener);
    }

    /**
     * Creates a new {@link TwilioConversationsClient}.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling token lifetime
     * @param listener A listener that receive events from the TwilioConversationsClient.
     *
     * @return the initialized {@link TwilioConversationsClient}, or null if the Twilio Conversations Client
     *         was not initialized
     */
    public static TwilioConversationsClient createConversationsClient(TwilioAccessManager accessManager,
                                                                      ClientOptions options,
                                                                      ConversationsClientListener listener) {
        if (accessManager == null) {
            throw new NullPointerException("access manager must not be null");
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

    /**
     * Returns the version of the Twilio Conversations SDK.
     *
     * @return the version of the SDK
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
