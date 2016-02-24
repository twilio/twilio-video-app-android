package com.twilio.conversations;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.conversations.impl.TwilioConversationsImpl;

public class TwilioConversations {
    /**
     *  Authenticating your Client failed due to invalid auth credentials.
     */
    public final static int INVALID_AUTH_DATA = 100;

    /**
     *  The SIP account was invalid.
     */
    public final static int INVALID_SIP_ACCOUNT = 102;

    /**
     *  There was an error during Client registration.
     */
    public final static int CLIENT_REGISTATION_ERROR= 103;

    /**
     *  The Conversation was invalid.
     */
    public final static int INVALID_CONVERSATION = 105;

    /**
     *  The Client you invited was not available.
     */
    public final static int CONVERSATION_PARTICIPANT_NOT_AVAILABLE = 106;

    /**
     *  The Client rejected your invitation.
     */
    public final static int CONVERSATION_REJECTED = 107;

    /**
     *  The Client was busy; and could not handle your invitation.
     */
    public final static int CONVERSATION_IGNORED = 108;

    /**
     *  The Conversation failed to start.
     */
    public final static int CONVERSATION_FAILED = 109;

    /**
     *  The Conversation was terminated due to an unforeseen error.
     */
    public final static int CONVERSATION_TERMINATED = 110;

    /**
     *  Establishing a media connection with the remote peer failed.
     */
    public final static int PEER_CONNECTION_FAILED = 111;

    /**
     *  The remote client address was invalid.
     */
    public final static int INVALID_PARTICIPANT_ADDRESSES = 112;

    /**
     *  The client disconnected unexpectedly.
     */
    public final static int CLIENT_DISCONNECTED = 200;

    /**
     *  Too many active Conversations.
     */
    public final static int TOO_MANY_ACTIVE_CONVERSATIONS = 201;

    /**
     *  Too many tracks were added to the local media.
     *  @note: The current maximum is one video track at a time.
     */
    public final static int TOO_MANY_TRACKS = 300;

    /**
     *  An invalid video capturer was added to the local media
     *  @note: At the moment, only {@link CameraCapturer} is supported.
     */
    public final static int INVALID_VIDEO_CAPTURER = 301;

    /**
     *  An attempt was made to add or remove a track that is already being operated on.
     *  @note: Retry your request at a later time.
     */
    public final static int TRACK_OPERATION_IN_PROGRESS = 303;

    /**
     *  An attempt was made to remove a track that has already ended.
     *  @note: The video track is in the {@link MediaTrackState} ENDED state.
     */
    public final static int INVALID_VIDEO_TRACK_STATE = 305;

    /**
     * Interface for the listener object to pass to
     * {@link TwilioConversations#initialize(Context, InitListener)}.
     */
    public interface InitListener {
        /**
         * Callback to report when Twilio Conversations SDK
         * has been successfully initialized.
         */
        void onInitialized();

        /**
         * Called if there is an error initializing the Twilio
         * Conversations SDK.
         *
         * @param exception An exception describing the error that occurred
         */
        void onError(Exception exception);
    }

    /**
     * Log levels for the Twilio Conversations SDK
     */
    public final class LogLevel {
        public static final int DISABLED = 0;
        public static final int ERROR = 3;
        public static final int WARNING= 4;
        public static final int INFO = 6;
        public static final int DEBUG = 7;
        public static final int VERBOSE = 8;
    }

    private TwilioConversations() {}

    /**
     * Initialize the Twilio Conversations SDK.
     *
     * @param context
     *            The application context of your Android application
     *
     * @param initListener
     *            A {@link TwilioConversations.InitListener} that will notify you
     *            when the service is ready
     *
     */
    public static void initialize(Context context,
                                  TwilioConversations.InitListener initListener) {
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
     * all {@link ConversationsClient} are destroyed and are no longer usable.
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
    public static int getLogLevel() {
        return TwilioConversationsImpl.getLogLevel();
    }

    /**
     * Sets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(int level) {
        TwilioConversationsImpl.setLogLevel(level);
    }

    /**
     * Creates a new ConversationsClient.
     *
     * @param token The access token.
     * @param listener A listener that receives events from the conversations client.
     *
     * @return the initialized {@link ConversationsClient}, or null if the Twilio Conversations Client
     *         was not initialized
     */
    public static ConversationsClient createConversationsClient(String token,
                                                                ConversationsClientListener listener) {
        if (token == null) {
            throw new NullPointerException("token must not be null");
        }
        TwilioAccessManager manager = TwilioAccessManagerFactory.createAccessManager(token, null);

        return createConversationsClient(manager, listener);
    }

    /**
     * Creates a new {@link ConversationsClient}.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling token lifetime
     * @param listener A listener that receive events from the ConversationsClient.
     *
     * @return the initialized {@link ConversationsClient}, or null if the Twilio Conversations Client
     *         was not initialized
     */
    public static ConversationsClient createConversationsClient(TwilioAccessManager accessManager,
                                                                ConversationsClientListener listener) {
        return createConversationsClient(accessManager, new HashMap<String, String>(), listener);
    }

    /**
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

    /**
     * Returns the version of the Twilio Conversations SDK.
     *
     * @return the version of the SDK
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
