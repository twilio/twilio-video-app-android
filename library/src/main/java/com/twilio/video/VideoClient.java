package com.twilio.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.getkeepsafe.relinker.ReLinker;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The VideoClient allows a user to connect to a Room.
 */
public class VideoClient {
    private static LogLevel level = LogLevel.OFF;
    private static Map<LogModule, LogLevel> moduleLogLevel = new EnumMap(LogModule.class);
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(VideoClient.class);

    private final Handler handler;
    private final Context applicationContext;
    private final Set<Room> rooms = new HashSet<>();
    private NetworkInfo currentNetworkInfo = null;

    private final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager conn =  (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo newNetworkInfo = conn.getActiveNetworkInfo();
                NetworkChangeEvent networkChangeEvent = NetworkChangeEvent.CONNECTION_CHANGED;

                if ((newNetworkInfo != null) &&
                    (currentNetworkInfo == null ||
                     currentNetworkInfo.getDetailedState() != newNetworkInfo.getDetailedState() ||
                     currentNetworkInfo.getType() != newNetworkInfo.getType() ||
                     currentNetworkInfo.getSubtype() != newNetworkInfo.getSubtype())){
                    if (!newNetworkInfo.isConnectedOrConnecting()) {
                        networkChangeEvent = NetworkChangeEvent.CONNECTION_LOST;
                    }
                    logger.d("Network event detected: " + networkChangeEvent.name());
                    nativeOnNetworkChange(nativeClientContext, networkChangeEvent);
                } else if (newNetworkInfo == null) {
                    networkChangeEvent = NetworkChangeEvent.CONNECTION_LOST;
                    logger.d("Network connection lost");
                    nativeOnNetworkChange(nativeClientContext, networkChangeEvent);
                }
                currentNetworkInfo = newNetworkInfo;
            }
        }
    };

    private String token;
    private long nativeClientContext;

    public VideoClient(Context context, String token) {
        if (context == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (token == null) {
            throw new NullPointerException("Token must not be null");
        }

        this.applicationContext = context.getApplicationContext();
        this.token = token;
        this.handler = Util.createCallbackHandler();

        if (!libraryIsLoaded) {
            ReLinker.loadLibrary(this.applicationContext, "jingle_peerconnection_so");
            libraryIsLoaded = true;
        }

        /*
         * The user may have set the log level prior to the native library being loaded.
         * Attempt to set the core log level now that the native library has loaded.
         */
        trySetCoreLogLevel(level.ordinal());

        /*
         * It is possible that the user has tried to set the log level for a specific module
         * before the library has loaded. Here we apply the log level for the module because we
         * know the native library is available
         */
        for (LogModule module : moduleLogLevel.keySet()) {
            trySetCoreModuleLogLevel(module.ordinal(), moduleLogLevel.get(module).ordinal());
        }
    }

    /**
     * Sets the audio output speaker for the device.
     * <p>
     * Bluetooth headset is not supported.
     * </p>
     * To use volume up/down keys call
     * 'setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);' in your Android Activity.
     *
     * @param audioOutput that should be used by the system
     */
    public void setAudioOutput(AudioOutput audioOutput) {
        AudioManager audioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioOutput == AudioOutput.SPEAKERPHONE) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    /**
     * Audio output speaker for the current client device.
     *
     * @return audio output speaker.
     */
    public AudioOutput getAudioOutput() {
        AudioManager audioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn() ? AudioOutput.SPEAKERPHONE : AudioOutput.HEADSET;
    }

    /**
     * Connect to a {@link Room}.
     *
     * @param roomListener listener of room related events.
     * @return room being connected to.
     */
    public Room connect(Room.Listener roomListener) {
        if (roomListener == null) {
            throw new NullPointerException("roomListener must not be null");
        }
        ConnectOptions connectOptions = new ConnectOptions.Builder().build();
        return connect(connectOptions, roomListener);
    }

    /**
     * Connect to a {@link Room} with specified options.
     *
     * @param connectOptions options for connecting to room.
     * @param roomListener listener of room related events.
     * @return room being connected to.
     */
    public synchronized Room connect(ConnectOptions connectOptions, Room.Listener roomListener) {
        if (connectOptions == null) {
            throw new NullPointerException("connectOptions must not be null");
        }
        if (roomListener == null) {
            throw new NullPointerException("roomListener must not be null");
        }

        if(rooms.isEmpty()) {
            nativeClientContext = nativeCreateClient(applicationContext,
                    token,
                    MediaFactory.instance(applicationContext).getNativeMediaFactoryHandle());

            // Register for connectivity events
            registerConnectivityBroadcastReceiver();
            ConnectivityManager conn =  (ConnectivityManager)
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            currentNetworkInfo = conn.getActiveNetworkInfo();

        }

        // Update the token in case user has updated
        nativeUpdateToken(nativeClientContext, token);

        Room room = new Room(connectOptions.getRoomName(),
                connectOptions.getLocalMedia(),
                roomListenerProxy(roomListener),
                handler);

        rooms.add(room);

        /*
         * We need to synchronize access to room listener during initialization and make
         * sure that onConnect() callback won't get called before connect() exits and Room
         * creation is fully completed.
         */
        synchronized (room.getConnectLock()) {
            long nativeRoomContext = nativeConnect(nativeClientContext,
                    room.getListenerNativeHandle(), connectOptions);
            room.setNativeContext(nativeRoomContext);
            room.setState(RoomState.CONNECTING);
        }

        return room;
    }

    /**
     * Updates the access token.
     *
     * @param token The new access token.
     */
    public synchronized void updateToken(String token) {
        this.token = token;
    }

    synchronized void release(Room room) {
        rooms.remove(room);
        if (rooms.isEmpty() && nativeClientContext != 0) {
            // With no more room connections we can unregister for connectivity events
            unregisterConnectivityBroadcastReceiver();

            nativeRelease(nativeClientContext);
            nativeClientContext = 0;
        }
    }

    private Room.Listener roomListenerProxy(final Room.Listener roomListener) {
        return new Room.Listener() {

            @Override
            public void onConnected(Room room) {
                roomListener.onConnected(room);
            }

            @Override
            public void onConnectFailure(Room room, TwilioException twilioException) {
                roomListener.onConnectFailure(room, twilioException);
                release(room);
            }

            @Override
            public void onDisconnected(Room room, TwilioException twilioException) {
                roomListener.onDisconnected(room, twilioException);
                release(room);
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                roomListener.onParticipantConnected(room, participant);
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                roomListener.onParticipantDisconnected(room, participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                roomListener.onRecordingStarted(room);
            }

            @Override
            public void onRecordingStopped(Room room) {
                roomListener.onRecordingStopped(room);
            }
        };
    }

    /**
     * Returns the version of the Video SDK.
     *
     * @return the version of the SDK
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Gets the logging level for messages logged by the Video SDK.
     *
     * @return the logging level
     */
    public static LogLevel getLogLevel() {
        return LogLevel.values()[tryGetCoreLogLevel()];
    }

    /**
     * Sets the logging level for messages logged by the Video SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(LogLevel level) {
        setSDKLogLevel(level);
        trySetCoreLogLevel(level.ordinal());
        // Save the log level
        VideoClient.level = level;
    }

    /**
     * Sets the logging level for messages logged by a specific module.
     *
     * @param module The module for this log level
     * @param level  The logging level
     */
    public static void setModuleLogLevel(LogModule module, LogLevel level) {
        if (module == LogModule.PLATFORM) {
            setSDKLogLevel(level);
        }
        trySetCoreModuleLogLevel(module.ordinal(), level.ordinal());
        // Save the module log level
        VideoClient.moduleLogLevel.put(module, level);
    }

    private static void setSDKLogLevel(LogLevel level) {
         /*
         * The Log Levels are defined differently in the Logger
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

    private void registerConnectivityBroadcastReceiver() {
        applicationContext.registerReceiver(connectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterConnectivityBroadcastReceiver() {
        applicationContext.unregisterReceiver(connectivityChangeReceiver);
    }

    enum NetworkChangeEvent {
        CONNECTION_LOST,
        CONNECTION_CHANGED
    }

    private native static void nativeSetCoreLogLevel(int level);
    private native static void nativeSetModuleLevel(int module, int level);
    private native static int nativeGetCoreLogLevel();
    private native long nativeCreateClient(Context context,
                                           String token,
                                           long nativeMediaFactoryHandle);
    private native long nativeConnect(long nativeClientDataHandler,
                                      long nativeRoomListenerHandle,
                                      ConnectOptions ConnectOptions);
    private native void nativeUpdateToken(long nativeClientContext, String token);
    private native void nativeOnNetworkChange(long nativeClientContext,
                                              NetworkChangeEvent networkChangeEvent);
    private native void nativeRelease(long nativeClientDataHandler);
}
