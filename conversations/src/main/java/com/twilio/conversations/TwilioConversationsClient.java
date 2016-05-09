package com.twilio.conversations;

import android.content.Context;
import android.os.Handler;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.impl.TwilioConversationsClientInternal;
import com.twilio.conversations.impl.TwilioConversationsImpl;

import java.util.Set;

/**
 * TwilioConversationsClient allows user to create or participate in conversations.
 *
 * @see ConversationsClientListener
 */
public class TwilioConversationsClient {
    /**
     *  Authenticating your Client failed due to invalid auth credentials.
     */
    public static int INVALID_AUTH_DATA = 100;
    /**
     *  The SIP account was invalid.
     */
    public static int INVALID_SIP_ACCOUNT = 102;
    /**
     *  There was an error during Client registration.
     */
    public static int CLIENT_REGISTATION_ERROR= 103;
    /**
     *  The Conversation was invalid.
     */
    public static int INVALID_CONVERSATION = 105;
    /**
     *  The Client you invited was not available.
     */
    public static int CONVERSATION_PARTICIPANT_NOT_AVAILABLE = 106;
    /**
     *  The Client rejected your invitation.
     */
    public static int CONVERSATION_REJECTED = 107;
    /**
     *  The Client was busy; and could not handle your invitation.
     */
    public static int CONVERSATION_IGNORED = 108;
    /**
     *  The Conversation failed to start.
     */
    public static int CONVERSATION_FAILED = 109;
    /**
     *  The Conversation was terminated due to an unforeseen error.
     */
    public static int CONVERSATION_TERMINATED = 110;
    /**
     *  Establishing a media connection with the remote peer failed.
     */
    public static int PEER_CONNECTION_FAILED = 111;
    /**
     *  The remote client address was invalid.
     */
    public static int INVALID_PARTICIPANT_ADDRESSES = 112;
    /**
     *  The client disconnected unexpectedly.
     */
    public static int CLIENT_DISCONNECTED = 200;
    /**
     *  Too many active Conversations.
     */
    public static int TOO_MANY_ACTIVE_CONVERSATIONS = 201;
    /**
     *  A track was created with constraints that could not be satisfied.
     */
    public static int TRACK_CREATION_FAILED = 207;
    /**
     *  Too many tracks were added to the local media.
     *  @note: The current maximum is one video track at a time.
     */
    public static int TOO_MANY_TRACKS = 300;
    /**
     *  An invalid video capturer was added to the local media
     *  @note: At the moment, only {@link CameraCapturer} is supported.
     */
    public static int INVALID_VIDEO_CAPTURER = 301;
    /**
     *  An attempt was made to add or remove a track that is already being operated on.
     *  @note: Retry your request at a later time.
     */
    public static int TRACK_OPERATION_IN_PROGRESS = 303;
    /**
     *  An attempt was made to remove a track that has already ended.
     *  @note: The video track is in the {@link MediaTrackState} ENDED state.
     */
    public static int INVALID_VIDEO_TRACK_STATE = 305;

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
    public static LogLevel getLogLevel() {
        return TwilioConversationsImpl.getLogLevel();
    }

    /**
     * Sets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(LogLevel level) {
        TwilioConversationsImpl.setLogLevel(level);
    }

    /**
     * Sets the logging level for messages logged by a specific module.
     *
     * @param module The module for this log level
     * @param level The logging level
     */
    public static void setModuleLogLevel(LogModule module, LogLevel level) {
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

        TwilioConversationsClient client = new TwilioConversationsClient(
                conversationsSdk.createConversationsClient(accessManager, options, listener));
        return client;
    }

    /**
     * Returns the version of the Twilio Conversations SDK.
     *
     * @return the version of the SDK
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Set a new {@link ConversationsClientListener} object to respond to client events.
     *
     * @param listener A listener for client events.
     */
    public void setConversationsClientListener(ConversationsClientListener listener){
        conversationsClientInternal.setConversationsClientListener(listener);
    }

    /**
     * Get identity of this conversations client on the network.
     *
     * @return identity of this conversations client
     */
    public String getIdentity(){return conversationsClientInternal.getIdentity();}

    /**
     * Reflects current listening state of the conversations client.
     *
     * @return <code>true</code> if conversations client is listening, </code>false</code>
     * otherwise.
     */
    public boolean isListening(){return conversationsClientInternal.isListening();}

    /**
     * Starts listening for incoming invites and allows outgoing invites to be sent.
     *
     * <p>The result of this method will propagate via the {@link ConversationsClientListener}:</p>
     * <ol>
     *     <li>{@link ConversationsClientListener#onStartListeningForInvites(TwilioConversationsClient)}
     *     will be invoked if the client is listening for invites</li>
     *     <li>{@link ConversationsClientListener#onFailedToStartListening(TwilioConversationsClient,
     *     TwilioConversationsException)} (ConversationsClient)} will be invoked if an issue
     *     occurred while attempting to listen</li>
     * </ol>
     */
    public void listen(){conversationsClientInternal.listen();}

    /**
     * Stops listening for incoming conversations.
     *
     * <p>{@link ConversationsClientListener#onStopListeningForInvites(TwilioConversationsClient)}
     * will be invoked upon the completion of this process</p>
     */
    public void unlisten(){conversationsClientInternal.unlisten();}

    /**
     * Sends an invitation to start a conversation with the following participants and local media
     * configuration. The {@link TwilioConversationsClient} must be listening before sending an
     * outgoing invite.
     *
     * <p>The result of this method will propagate via the {@link ConversationCallback} provided
     * according to the following scenarios:</p>
     *
     * <ol>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with a <code>null</code> value of
     *     {@link TwilioConversationsException} if the invite was accepted</li>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with one of the following error codes for
     *     the returned {@link TwilioConversationsException}</li>
     *     <ul>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED}</li>
     *     </ul>
     * </ol>
     *
     * @param participants Set of participant names as Strings
     * @param localMedia Local Media you would like to use when setting up the new conversation
     * @param conversationCallback The callback that will provide the conversation once it's been
     *                             created
     */
    public OutgoingInvite sendConversationInvite(Set<String> participants,
                                          LocalMedia localMedia,
                                          ConversationCallback conversationCallback) {
        return conversationsClientInternal.sendConversationInvite(
                participants, localMedia, conversationCallback);
    }

    /**
     * Sends an invitation to start a conversation with the following participants and local media
     * configuration. The {@link TwilioConversationsClient} must be listening before sending an
     * outgoing invite.
     *
     * <p>The result of this method will propagate via the {@link ConversationCallback} provided
     * according to the following scenarios:</p>
     *
     * <ol>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with a <code>null</code> value of
     *     {@link TwilioConversationsException} if the invite was accepted</li>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with one of the following error codes for
     *     the returned {@link TwilioConversationsException}</li>
     *     <ul>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED}</li>
     *     </ul>
     * </ol>
     *
     * @param participants Set of participant names as Strings
     * @param localMedia Local Media you would like to use when setting up the new conversation
     * @param iceOptions Custom ICE (Interactive Connectivity Establishment) protocol options
     * @param conversationCallback The callback that will provide the conversation once it's been
     *                             created
     */
    public OutgoingInvite sendConversationInvite(Set<String> participants,
                                          LocalMedia localMedia,
                                          IceOptions iceOptions,
                                          ConversationCallback conversationCallback) {
        return conversationsClientInternal.sendConversationInvite(
                participants, localMedia, iceOptions, conversationCallback);
    }

    /**
     * Sets the audio output speaker for the device.
     *
     * Bluetooth headset is not supported.
     *
     * To use volume up/down keys call
     * 'setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);' in your Android Activity.
     *
     * @param audioOutput that should be used by the system
     */
    public void setAudioOutput(AudioOutput audioOutput) {
        conversationsClientInternal.setAudioOutput(audioOutput);
    }

    /**
     * Audio output speaker for the current client device
     *
     * @return audio output speaker
     */
    public AudioOutput getAudioOutput() {return conversationsClientInternal.getAudioOutput();}
    

    /**
     * Interface for the listener object to pass to
     * {@link TwilioConversationsClient#initialize(Context, InitListener)}.
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


    // Private

    private TwilioConversationsClientInternal conversationsClientInternal;

    private TwilioConversationsClient(
            TwilioConversationsClientInternal conversationsClientInternal) {
        this.conversationsClientInternal = conversationsClientInternal;
        this.conversationsClientInternal.setTwilioConversationsClient(this);
    }

}
