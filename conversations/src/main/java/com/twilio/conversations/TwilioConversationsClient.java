package com.twilio.conversations;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.internal.Logger;
import com.twilio.conversations.internal.ReLinker;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TwilioConversationsClient allows user to create or participate in conversations.
 *
 * @see Listener
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
     * Listener interface defines a set of callbacks for events related to a
     * {@link TwilioConversationsClient}.
     *
     */
    public static interface Listener {
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
         * This method will be invoked after a successful call to
         * {@link TwilioConversationsClient#unlisten()} or when a network change occurs.
         *
         * @param twilioConversationsClient The client that is no longer listening for incoming
         *                                  invitations.
         */
        void onStopListeningForInvites(TwilioConversationsClient twilioConversationsClient);

        /**
         *
         * This method notifies the listener that the client failed to start listening for
         * invitations. This method is invoked after an unsuccessful call to
         * {@link TwilioConversationsClient#listen()}.
         *
         * @param twilioConversationsClient The conversations client that failed to start listening
         *                                  for incoming invitations.
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
        void onIncomingInvite(TwilioConversationsClient twilioConversationsClient,
                              IncomingInvite incomingInvite);

        /**
         * This method notifies the listener that the incoming invite was cancelled.
         *
         * @param incomingInvite The invitation object.
         */
        void onIncomingInviteCancelled(TwilioConversationsClient twilioConversationsClient,
                                       IncomingInvite incomingInvite);
    }

    /**
     * Initialize the Twilio Conversations SDK.
     *
     * @param context
     *            The application context of your Android application
     *
     */
    public static void initialize(Context context) {
        if (context == null) {
            throw new NullPointerException("applicationContext must not be null");
        }

        if (level != LogLevel.OFF) {
            // Re-apply the log level. Initialization sets a default log level.
            setLogLevel(level);
        }

        if (initialized) {
            return;
        }

        Context applicationContext = context.getApplicationContext();
        Handler handler = Util.createCallbackHandler();

        internalRegistry = new InternalRegistry(applicationContext, handler);
        checkPermissions(context);


        /*
         * With all the invariants satisfied we can now load the library if we have not done so
         */
        if (!libraryIsLoaded) {
            ReLinker.loadLibrary(applicationContext, "jingle_peerconnection_so");
            libraryIsLoaded = true;
        }

        /*
         * It is possible that the user has tried to set the log level before the native library
         * has loaded. Here we apply the log level because we know the native library is available
         */
        if (level != LogLevel.OFF) {
            trySetCoreLogLevel(level.ordinal());
        }

        /*
         * It is possible that the user has tried to set the log level for a specific module
         * before the library has loaded. Here we apply the log level for the module because we
         * know the native library is available
         */
        for (LogModule module : moduleLogLevel.keySet()) {
            trySetCoreModuleLogLevel(module.ordinal(), moduleLogLevel.get(module).ordinal());
        }

        boolean success = nativeInitCore(applicationContext);
        if (!success) {
            initialized = false;
            throw new RuntimeException("Twilio conversations failed to initialize.");
        }
        internalRegistry.setupLifecycleListeners();
        initialized = true;
    }

    /**
     * Dispose the Twilio Conversations SDK. Note that once this completes
     * all {@link TwilioConversationsClient} are destroyed and are no longer usable.
     *
     */
    public synchronized static void destroy() {
        if (initialized) {
            // Destoy all registered clients and system events listeners
            internalRegistry.destroy();
            internalRegistry = null;

            // Now we can teardown the sdk
            // TODO destroy investigate making this asynchronous with callbacks
            logger.d("Destroying Core");
            nativeDestroyCore();
            logger.d("Core destroyed");
            initialized = false;
        }
    }

    /**
     * Informs whether {@link TwilioConversationsClient} is initialized or not.
     *
     * @return <code>true</code> if Twilio Conversations client is initialized,
     *         <code>false</code> otherwise.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @return the logging level
     */
    public static LogLevel getLogLevel() {
        return LogLevel.values()[tryGetCoreLogLevel()];
    }

    /**
     * Sets the logging level for messages logged by the Twilio Conversations SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(LogLevel level) {
        setSDKLogLevel(level);
        trySetCoreLogLevel(level.ordinal());
        // Save the log level
        TwilioConversationsClient.level = level;
    }

    /**
     * Sets the logging level for messages logged by a specific module.
     *
     * @param module The module for this log level
     * @param level The logging level
     */
    public static void setModuleLogLevel(LogModule module, LogLevel level) {
        if (module == LogModule.PLATFORM) {
            setSDKLogLevel(level);
        }
        trySetCoreModuleLogLevel(module.ordinal(), level.ordinal());
        //Save the module log level
        TwilioConversationsClient.moduleLogLevel.put(module, level);
    }

    /**
     * Creates a new {@link TwilioConversationsClient}.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling token
     *                      lifetime
     * @param listener A listener that receive events from the TwilioConversationsClient.
     *
     * @return the initialized {@link TwilioConversationsClient}, or null if the Twilio
     *         Conversations Client was not initialized
     */
    public static TwilioConversationsClient create(TwilioAccessManager accessManager,
                                                   Listener listener) {
        return create(accessManager, null, listener);
    }

    /**
     * Creates a new {@link TwilioConversationsClient}.
     *
     * @param accessManager The instance of {@link TwilioAccessManager} that is handling
     *                      token lifetime
     * @param listener A listener that receive events from the TwilioConversationsClient.
     *
     * @return the initialized {@link TwilioConversationsClient}, or null if the Twilio
     *         Conversations Client was not initialized
     */
    public static TwilioConversationsClient create(TwilioAccessManager accessManager,
                                                   ClientOptions options,
                                                   Listener listener) {
        if (accessManager == null) {
            throw new NullPointerException("access manager must not be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }

        if (!initialized || (internalRegistry == null)) {
            throw new IllegalStateException("Cannot create client before initialize is called");
        }

        TwilioConversationsClient client =
                new TwilioConversationsClient(accessManager, options, listener);

        internalRegistry.registerTwilioConversationClient(client);

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
     * Set a new {@link Listener} object to respond to client events.
     *
     * @param listener A listener for client events.
     */
    public void setListener(Listener listener){
        conversationsClientInternal.setConversationsClientListener(listener);
    }

    /**
     * Get identity of this conversations client on the network.
     *
     * @return identity of this conversations client
     *         or null if an invalid TwilioAccessManager was provided
     */
    public String getIdentity(){
        return conversationsClientInternal.getIdentity();
    }

    /**
     * Reflects current listening state of the conversations client.
     *
     * @return <code>true</code> if conversations client is listening, </code>false</code>
     * otherwise.
     */
    public boolean isListening(){
        return conversationsClientInternal.isListening();
    }

    /**
     * Starts listening for incoming invites and allows outgoing invites to be sent.
     *
     * <p>The result of this method will propagate via the {@link Listener}:</p>
     * <ol>
     *     <li>{@link Listener#onStartListeningForInvites(TwilioConversationsClient)}
     *     will be invoked if the client is listening for invites</li>
     *     <li>{@link Listener#onFailedToStartListening(TwilioConversationsClient,
     *     TwilioConversationsException)} (ConversationsClient)} will be invoked if an issue
     *     occurred while attempting to listen</li>
     * </ol>
     */
    public void listen(){
        conversationsClientInternal.listen();
    }

    /**
     * Stops listening for incoming conversations.
     *
     * <p>{@link Listener#onStopListeningForInvites(TwilioConversationsClient)}
     * will be invoked upon the completion of this process</p>
     */
    public void unlisten(){
        conversationsClientInternal.unlisten();
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
     * @param conversationCallback The callback that will provide the conversation once it's been
     *                             created
     */
    public OutgoingInvite inviteToConversation(Set<String> participants,
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
    public OutgoingInvite inviteToConversation(Set<String> participants,
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
    public AudioOutput getAudioOutput() {
        return conversationsClientInternal.getAudioOutput();
    }


    // Private

    private TwilioConversationsClientInternal conversationsClientInternal;
    private static volatile InternalRegistry internalRegistry;

    private static final int REQUEST_CODE_WAKEUP = 100;
    private static final long BACKGROUND_WAKEUP_INTERVAL = 10 * 60 * 1000;

    private static LogLevel level = LogLevel.OFF;
    private static Map<LogModule, LogLevel> moduleLogLevel = new EnumMap<LogModule, LogLevel>
            (LogModule.class);

    private static volatile boolean libraryIsLoaded = false;

    static final Logger logger = Logger.getLogger(TwilioConversationsClient.class);

    private static boolean initialized;

    private final UUID uuid = UUID.randomUUID();


    private TwilioConversationsClient(TwilioAccessManager accessManager,
                                      ClientOptions options,
                                      Listener listener) {
        this.conversationsClientInternal = new TwilioConversationsClientInternal(
                this,
                internalRegistry.applicationContext,
                accessManager,
                listener,
                options,
                internalRegistry.handler);
    }

    private static void setSDKLogLevel(LogLevel level) {
         /*
         * The Log Levels are defined differently in the Twilio Logger
         * which is based off android.util.Log.
         */
        switch (level) {
            case OFF:
                Logger.setLogLevel(Log.ASSERT);
                break;
            case FATAL:
                Logger.setLogLevel(Log.ERROR);
                break;
            case ERROR:
                Logger.setLogLevel(Log.ERROR);
                break;
            case WARNING:
                Logger.setLogLevel(Log.WARN);
                break;
            case INFO:
                Logger.setLogLevel(Log.INFO);
                break;
            case DEBUG:
                Logger.setLogLevel(Log.DEBUG);
                break;
            case TRACE:
                Logger.setLogLevel(Log.VERBOSE);
                break;
            case ALL:
                Logger.setLogLevel(Log.VERBOSE);
                break;
            default:
                // Set the log level to assert/disabled if the value passed in is unknown
                Logger.setLogLevel(Log.ASSERT);
                break;
        }
    }

    /*
     * This is a convenience safety method in the event that the core log level is attempted before
     * initialization.
     *
     * @param level
     */
    private static void trySetCoreLogLevel(int level) {
        if (libraryIsLoaded) {
            nativeSetCoreLogLevel(level);
        }
    }

    /*
     * Convenience safety method for retrieving core log level.
     *
     * @return Core log level or current value that the user has set if the native library has not
     * been loaded
     */
    private static int tryGetCoreLogLevel() {
        return (libraryIsLoaded) ? (nativeGetCoreLogLevel()) : (level.ordinal());
    }


    private static void trySetCoreModuleLogLevel(int module, int level) {
        if (libraryIsLoaded) {
            nativeSetModuleLevel(module, level);
        }
    }

    /*
     * TODO
     * Technically the SDK should be able to work with all
     * normal permissions. With Android 23 and up, the user could
     * opt out of dangerous permissions at any time. The SDK should
     * adjust accordingly.
     **/
    private static final String[] requiredPermissions = {
            // Dangerous permissions (require permission)
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",

            // Normal permissions (granted upon install)
            "android.permission.INTERNET",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE"
    };

    private static void checkPermissions(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS
                            | PackageManager.GET_SERVICES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Unable to resolve permissions. " + e.getMessage());
        }

        // Check application permissions
        Map<String, Boolean> appPermissions = new HashMap<String, Boolean>(
                pinfo.requestedPermissions != null ? pinfo.requestedPermissions.length
                        : 0);
        if (pinfo.requestedPermissions != null) {
            for (String permission : pinfo.requestedPermissions)
                appPermissions.put(permission, true);
        }

        List<String> missingPermissions = new LinkedList<String>();
        for (String permission : requiredPermissions) {
            if (!appPermissions.containsKey(permission))
                missingPermissions.add(permission);
        }

        if (!missingPermissions.isEmpty()) {
            StringBuilder builder = new StringBuilder(
                    "Your app is missing the following required permissions:");
            for (String permission : missingPermissions)
                builder.append(' ').append(permission);

            throw new RuntimeException(builder.toString());
        }
    }



    // InternalRegistry
    private static class InternalRegistry {
        public final Context applicationContext;
        public final Handler handler;

        private PendingIntent wakeUpPendingIntent;

        private final ApplicationForegroundTracker applicationForegroundTracker =
                new ApplicationForegroundTracker();

        private class ConnectivityChangeReceiver extends BroadcastReceiver {

            private ExecutorService refreshRegExecutor = Executors.newSingleThreadExecutor();

            public ConnectivityChangeReceiver(){}

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    refreshRegExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            onNetworkChange();
                        }
                    });
                }
            }
        }

        private final ConnectivityChangeReceiver connectivityChangeReceiver =
                new ConnectivityChangeReceiver();

        /*
         * TODO
         * Currently this is needed to see if we have actually registered our receiver upon destruction
         * of the SDK. However this is only enabled when a client is created. Should this just
         * be tracked in TwilioConversationsClient?
         */
        private boolean observingConnectivity = false;

        protected final Map<UUID, TwilioConversationsClient>
                conversationsClientMap = new ConcurrentHashMap<>();



        public InternalRegistry(Context applicationContext, Handler handler) {
            this.applicationContext = applicationContext;
            this.handler = handler;
        }

        public void setupLifecycleListeners() {
            Application application = (Application) internalRegistry.applicationContext;
            AlarmManager alarmManager = (AlarmManager) applicationContext
                    .getSystemService(Context.ALARM_SERVICE);

            // Wake up periodically to refresh connections
            wakeUpPendingIntent = PendingIntent.getBroadcast(applicationContext,
                    REQUEST_CODE_WAKEUP,
                    new Intent(applicationContext, WakeUpReceiver.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    BACKGROUND_WAKEUP_INTERVAL,
                    BACKGROUND_WAKEUP_INTERVAL,
                    wakeUpPendingIntent);

            // Give hints to the core on application visibility events
            application.registerActivityLifecycleCallbacks(applicationForegroundTracker);
        }


        public void destroy() {
            if (observingConnectivity) {
                // No need to monitor network changes anymore
                unregisterConnectivityBroadcastReceiver();
            }

            // TODO: make this async again after debugging

            Queue<TwilioConversationsClient> clientsDisposing = new ArrayDeque<>();
            Application application = (Application)
                    applicationContext.getApplicationContext();
            AlarmManager alarmManager = (AlarmManager) applicationContext
                    .getSystemService(Context.ALARM_SERVICE);

            alarmManager.cancel(wakeUpPendingIntent);
            application.unregisterActivityLifecycleCallbacks(applicationForegroundTracker);

            // Process clients and determine which ones need to be closed
            for (Map.Entry<UUID, TwilioConversationsClient> entry :
                    conversationsClientMap.entrySet()) {
                TwilioConversationsClient client =
                        conversationsClientMap.remove(entry.getKey());
                if (client != null) {
                    // Dispose of the client regardless of whether it is still listening.
                    client.conversationsClientInternal.disposeClient();
                }
            }
        }


        public void registerTwilioConversationClient(TwilioConversationsClient client) {
            if (conversationsClientMap.size() == 0) {
                registerConnectivityBroadcastReceiver();
            }
            conversationsClientMap.put(client.uuid, client);
        }


        private void onNetworkChange() {
            if (initialized && (conversationsClientMap.size() > 0)) {
                // Only refresh registrations if there are clients that are listening
                for (Map.Entry<UUID, TwilioConversationsClient> entry :
                        conversationsClientMap.entrySet()) {
                    if(entry.getValue().isListening()) {
                        nativeRefreshRegistrations();
                        break;
                    }
                }
            }
        }

        private void registerConnectivityBroadcastReceiver() {
            if (applicationContext != null) {
                applicationContext.registerReceiver(connectivityChangeReceiver,
                        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                observingConnectivity = true;
            }
        }

        private void unregisterConnectivityBroadcastReceiver() {
            if (applicationContext != null && connectivityChangeReceiver != null) {
                applicationContext.unregisterReceiver(connectivityChangeReceiver);
                observingConnectivity = false;
            }
        }


    }

    private native static boolean nativeInitCore(Context context);
    private native static void nativeDestroyCore();
    private native static void nativeRefreshRegistrations();
    private native static void nativeSetCoreLogLevel(int level);
    private native static void nativeSetModuleLevel(int module, int level);
    private native static int nativeGetCoreLogLevel();

}

