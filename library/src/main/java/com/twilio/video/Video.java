/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.getkeepsafe.relinker.ReLinker;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** This class allows a user to connect to a Room. */
public abstract class Video {
    private static LogLevel level = LogLevel.OFF;
    private static Map<LogModule, LogLevel> moduleLogLevel = new EnumMap<>(LogModule.class);
    private static volatile boolean libraryIsLoaded = false;
    private static final Logger logger = Logger.getLogger(Video.class);

    private static final Set<Room> rooms = new HashSet<>();
    private static NetworkInfo currentNetworkInfo = null;
    private static Context applicationContext = null;

    private static final BroadcastReceiver connectivityChangeReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction()
                            .equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        if (isInitialStickyBroadcast()) {
                            logger.d("Ignoring network event, sticky broadcast");
                            return;
                        }
                        ConnectivityManager conn =
                                (ConnectivityManager)
                                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo newNetworkInfo = conn.getActiveNetworkInfo();
                        NetworkChangeEvent networkChangeEvent =
                                NetworkChangeEvent.CONNECTION_CHANGED;

                        if ((newNetworkInfo != null)
                                && (currentNetworkInfo == null
                                        || currentNetworkInfo.getDetailedState()
                                                != newNetworkInfo.getDetailedState()
                                        || currentNetworkInfo.getType() != newNetworkInfo.getType()
                                        || currentNetworkInfo.getSubtype()
                                                != newNetworkInfo.getSubtype())) {
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
    @NonNull
    public static synchronized Room connect(
            @NonNull Context context,
            @NonNull ConnectOptions connectOptions,
            @NonNull Room.Listener roomListener) {
        Preconditions.checkNotNull(context, "context must not be null");
        Preconditions.checkNotNull(connectOptions, "connectOptions must not be null");
        Preconditions.checkNotNull(roomListener, "roomListener must not be null");

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

        if (rooms.isEmpty()) {
            // Register for connectivity events
            ConnectivityManager conn =
                    (ConnectivityManager)
                            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            currentNetworkInfo = conn.getActiveNetworkInfo();
            registerConnectivityBroadcastReceiver();
        }
        Room room =
                new Room(
                        applicationContext,
                        connectOptions.getRoomName(),
                        Util.createCallbackHandler(),
                        roomListenerProxy(roomListener));
        rooms.add(room);
        room.connect(connectOptions);
        return room;
    }

    static synchronized void release(Room room) {
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
            public void onConnected(@NonNull Room room) {
                roomListener.onConnected(room);
            }

            @Override
            public void onConnectFailure(
                    @NonNull Room room, @NonNull TwilioException twilioException) {
                roomListener.onConnectFailure(room, twilioException);
                release(room);
            }

            @Override
            public void onReconnecting(
                    @NonNull Room room, @NonNull TwilioException twilioException) {
                roomListener.onReconnecting(room, twilioException);
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                roomListener.onReconnected(room);
            }

            @Override
            public void onDisconnected(
                    @NonNull Room room, @Nullable TwilioException twilioException) {
                roomListener.onDisconnected(room, twilioException);
                release(room);
            }

            @Override
            public void onParticipantConnected(
                    @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                roomListener.onParticipantConnected(room, remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(
                    @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                roomListener.onParticipantDisconnected(room, remoteParticipant);
            }

            @Override
            public void onRecordingStarted(@NonNull Room room) {
                roomListener.onRecordingStarted(room);
            }

            @Override
            public void onRecordingStopped(@NonNull Room room) {
                roomListener.onRecordingStopped(room);
            }
        };
    }

    /**
     * Returns the version of the Video SDK.
     *
     * @return the version of the SDK
     */
    @NonNull
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Gets the logging level for messages logged by the Video SDK.
     *
     * @return the logging level
     */
    @NonNull
    public static LogLevel getLogLevel() {
        return LogLevel.values()[tryGetCoreLogLevel()];
    }

    /**
     * Sets the logging level for messages logged by the Video SDK.
     *
     * @param level The logging level
     */
    public static void setLogLevel(@NonNull LogLevel level) {
        Preconditions.checkNotNull(level, "LogLevel should not be null");
        setSDKLogLevel(level);
        trySetCoreLogLevel(level.ordinal());
        // Save the log level
        Video.level = level;
    }

    /**
     * Sets the logging level for messages logged by a specific module.
     *
     * @param module The module for this log level
     * @param level The logging level
     */
    public static void setModuleLogLevel(@NonNull LogModule module, @NonNull LogLevel level) {
        Preconditions.checkNotNull(module, "LogModule should not be null");
        Preconditions.checkNotNull(level, "LogLevel should not be null");

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
            applicationContext.registerReceiver(
                    connectivityChangeReceiver,
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

    private static native void nativeSetCoreLogLevel(int level);

    private static native void nativeSetModuleLevel(int module, int level);

    private static native int nativeGetCoreLogLevel();
}
