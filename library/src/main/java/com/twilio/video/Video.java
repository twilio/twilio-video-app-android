package com.twilio.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.getkeepsafe.relinker.ReLinker;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class allows a user to connect to a Room.
 */
public abstract class Video {
    private static LogLevel level = LogLevel.OFF;
    private static Map<LogModule, LogLevel> moduleLogLevel = new EnumMap(LogModule.class);
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(Video.class);

    private static final Set<Room> rooms = new HashSet<>();
    private static NetworkInfo currentNetworkInfo = null;
    private static Context applicationContext = null;

    private static final BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
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
                    onNetworkChange(networkChangeEvent);
                } else if (newNetworkInfo == null) {
                    networkChangeEvent = NetworkChangeEvent.CONNECTION_LOST;
                    logger.d("Network connection lost");
                    onNetworkChange(networkChangeEvent);
                }
                currentNetworkInfo = newNetworkInfo;
            }
        }
    };

    /**
     * Connect to a {@link Room} with specified options.
     *
     * @param connectOptions options for connecting to room.
     * @param roomListener listener of room related events.
     * @return room being connected to.
     */
    public static synchronized Room connect(Context context,
                                            ConnectOptions connectOptions,
                                            Room.Listener roomListener) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (connectOptions == null) {
            throw new NullPointerException("connectOptions must not be null");
        }
        if (roomListener == null) {
            throw new NullPointerException("roomListener must not be null");
        }

        // FIXME: we shouldn't be caching this, but otherwise we don't have
        // a way of unregistering broadcast receiver
        if (applicationContext == null) {
            applicationContext = context.getApplicationContext();
        }

        if (!libraryIsLoaded) {
            ReLinker.loadLibrary(applicationContext, "jingle_peerconnection_so");
            libraryIsLoaded = true;
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

        if(rooms.isEmpty()) {
            // Register for connectivity events
            registerConnectivityBroadcastReceiver();
            ConnectivityManager conn =  (ConnectivityManager)
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            currentNetworkInfo = conn.getActiveNetworkInfo();
        }
        Room room = new Room(connectOptions.getRoomName(),
                roomListenerProxy(roomListener),
                Util.createCallbackHandler());
        rooms.add(room);
        room.connect(applicationContext, connectOptions);
        return room;
    }

    synchronized static void release(Room room) {
        rooms.remove(room);
        if (rooms.isEmpty()) {
            // With no more room connections we can unregister for connectivity events
            unregisterConnectivityBroadcastReceiver();
            PlatformInfo.release();
        }
    }

    private static synchronized void onNetworkChange(NetworkChangeEvent networkChangeEvent) {
        for (Room room : rooms) {
            room.onNetworkChanged(networkChangeEvent);
        }
    }

    private static Room.Listener roomListenerProxy(final Room.Listener roomListener) {
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
        Video.level = level;
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
        Video.moduleLogLevel.put(module, level);
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

    private static void registerConnectivityBroadcastReceiver() {
        if (applicationContext != null) {
            applicationContext.registerReceiver(connectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private static void unregisterConnectivityBroadcastReceiver() {
        if (applicationContext != null) {
            applicationContext.unregisterReceiver(connectivityChangeReceiver);
        }
    }

    enum NetworkChangeEvent {
        CONNECTION_LOST,
        CONNECTION_CHANGED
    }

    private native static void nativeSetCoreLogLevel(int level);
    private native static void nativeSetModuleLevel(int module, int level);
    private native static int nativeGetCoreLogLevel();
}
