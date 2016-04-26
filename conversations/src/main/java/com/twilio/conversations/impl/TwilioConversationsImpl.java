package com.twilio.conversations.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.ClientOptions;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversations.LogLevel;
import com.twilio.conversations.TwilioConversations.LogModule;
import com.twilio.conversations.impl.logging.Logger;
import com.twilio.conversations.impl.util.CallbackHandler;
import com.twilio.conversations.internal.ReLinker;

public class TwilioConversationsImpl {
    private static final int REQUEST_CODE_WAKEUP = 100;
    private static final long BACKGROUND_WAKEUP_INTERVAL = 10 * 60 * 1000;

    static final Logger logger = Logger.getLogger(TwilioConversationsImpl.class);

    private static volatile TwilioConversationsImpl instance;

    private static volatile boolean libraryIsLoaded = false;
    private static LogLevel level = LogLevel.OFF;
    private static Map<LogModule, LogLevel> moduleLogLevel = new EnumMap<LogModule, LogLevel>
            (LogModule.class);
    protected Context applicationContext;
    private boolean initialized;
    private boolean initializing;
    private ExecutorService refreshRegExecutor = Executors.newSingleThreadExecutor();

    public static class WakeUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getInstance().isInitialized()) {
                getInstance().onApplicationWakeUp();
            }
        }
    }
    private PendingIntent wakeUpPendingIntent;
    private class ApplicationForegroundTracker implements Application.ActivityLifecycleCallbacks {
        private Activity currentActivity;
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            currentActivity = activity;
            onApplicationForeground();
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (currentActivity == null ||
                    currentActivity == activity) {
                onApplicationBackground();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
    private final ApplicationForegroundTracker applicationForegroundTracker =
            new ApplicationForegroundTracker();
    private class ConnectivityChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                refreshRegExecutor.execute( new Runnable() {
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

    /**
     * TODO
     * Currently this is needed to see if we have actually registered our receiver upon destruction
     * of the SDK. However this is only enabled when a client is created. Should this just
     * be tracked in ConversationsClient?
     */
    private boolean observingConnectivity = false;

    protected final Map<UUID, WeakReference<ConversationsClientImpl>> conversationsClientMap = new ConcurrentHashMap<>();

    /**
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

    public static TwilioConversationsImpl getInstance() {
        if (instance == null) {
            synchronized (TwilioConversationsImpl.class) {
                if (instance == null)
                    instance = new TwilioConversationsImpl();
            }
        }

        if(level != LogLevel.OFF) {
            // Re-apply the log level. Initialization sets a default log level.
            setLogLevel(level);
        }

        return instance;
    }

    TwilioConversationsImpl() {}

    public void initialize(final Context context,
                           final TwilioConversations.InitListener initListener) {
        if (isInitialized() || isInitializing()) {
            initListener.onError(new RuntimeException("Initialize already called"));
            return;
        }

        initializing = true;
        this.applicationContext = context.getApplicationContext();

        PackageManager pm = context.getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS
                            | PackageManager.GET_SERVICES);
        } catch (NameNotFoundException e) {
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

        final Handler handler = CallbackHandler.create();
        if(handler == null) {
            throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
        }

        /**
         * With all the invariants satisfied we can now load the library if we have not done so
         */
        if (!libraryIsLoaded) {
            ReLinker.loadLibrary(applicationContext, "jingle_peerconnection_so");
            libraryIsLoaded = true;
        }

        /**
         * It is possible that the user has tried to set the log level before the native library
         * has loaded. Here we apply the log level because we know the native library is available
         */
        if(level != LogLevel.OFF) {
            trySetCoreLogLevel(level.ordinal());
        }

        /**
         * It is possible that the user has tried to set the log level for a specific module
         * before the library has loaded. Here we apply the log level for the module because we
         * know the native library is available
         */
        for (LogModule module: moduleLogLevel.keySet()) {
            trySetCoreModuleLogLevel(module.ordinal(), moduleLogLevel.get(module).ordinal());
        }

        boolean success = initCore(applicationContext);
        if (!success) {
            initializing = false;
            initialized = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initListener.onError(new RuntimeException("Twilio conversations " +
                            "failed to initialize."));
                }
            });
        } else {
            Application application = (Application)
                    TwilioConversationsImpl.this.applicationContext;
            AlarmManager alarmManager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            // Wake up periodically to refresh connections
            wakeUpPendingIntent = PendingIntent.getBroadcast(context,
                    REQUEST_CODE_WAKEUP,
                    new Intent(context, WakeUpReceiver.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    BACKGROUND_WAKEUP_INTERVAL,
                    BACKGROUND_WAKEUP_INTERVAL,
                    wakeUpPendingIntent);

            // Give hints to the core on application visibility events
            application.registerActivityLifecycleCallbacks(applicationForegroundTracker);

            initialized = true;
            initializing = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initListener.onInitialized();
                }
            });
        }


    }

    public void destroy() {
        if (observingConnectivity) {
            // No need to monitor network changes anymore
            unregisterConnectivityBroadcastReceiver();
        }

        // TODO: make this async again after debugging

        Queue<ConversationsClientImpl> clientsDisposing = new ArrayDeque<>();
        Application application = (Application)
                TwilioConversationsImpl.this.applicationContext.getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) applicationContext
                .getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(wakeUpPendingIntent);
        application.unregisterActivityLifecycleCallbacks(applicationForegroundTracker);

        // Process clients and determine which ones need to be closed
        for (Map.Entry<UUID, WeakReference<ConversationsClientImpl>> entry :
                conversationsClientMap.entrySet()) {
            WeakReference<ConversationsClientImpl> weakClientRef =
                    conversationsClientMap.remove(entry.getKey());

            if (weakClientRef != null) {
                ConversationsClientImpl client = weakClientRef.get();
                if (client != null) {
                    // Dispose of the client regardless of whether it is still listening.
                    client.disposeClient();
                }
            }
        }

        // Now we can teardown the sdk
        // TODO destroy investigate making this asynchronous with callbacks
        logger.d("Destroying Core");
        destroyCore();
        logger.d("Core destroyed");
        initialized = false;
    }

    public ConversationsClientImpl createConversationsClient(TwilioAccessManager accessManager,
                                                             ClientOptions options,
                                                             ConversationsClientListener inListener) {
        if(accessManager != null) {
            final ConversationsClientImpl conversationsClient = new ConversationsClientImpl(applicationContext,
                    accessManager, inListener, options);
           if (conversationsClientMap.size() == 0) {
                registerConnectivityBroadcastReceiver();
            }
            conversationsClientMap.put(conversationsClient.getUuid(),
                    new WeakReference<>(conversationsClient));
            return conversationsClient;
        }
        return null;
    }

    public static void setLogLevel(LogLevel level) {
        setSDKLogLevel(level);
        trySetCoreLogLevel(level.ordinal());
        // Save the log level
        TwilioConversationsImpl.level = level;
    }

    public static void setModuleLogLevel(LogModule module, LogLevel level) {
        if (module == LogModule.PLATFORM) {
            setSDKLogLevel(level);
        }
        trySetCoreModuleLogLevel(module.ordinal(), level.ordinal());
        //Save the module log level
        TwilioConversationsImpl.moduleLogLevel.put(module, level);
    }

    private static void setSDKLogLevel(LogLevel level) {
         /*
         * The Log Levels are defined differently in the Twilio Logger
         * which is based off android.util.Log.
         */
        switch(level) {
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

    public static LogLevel getLogLevel() {
        return LogLevel.values()[tryGetCoreLogLevel()];
    }

    public boolean isInitialized() {
        return initialized;
    }

    boolean isInitializing() {
        return initializing;
    }

    public ConversationsClientImpl findDeviceByUUID(UUID uuid) {
        WeakReference<ConversationsClientImpl> deviceRef = conversationsClientMap.get(uuid);
        if (deviceRef != null) {
            ConversationsClientImpl device = deviceRef.get();
            if (device != null) {
                return device;
            } else {
                conversationsClientMap.remove(uuid);
            }
        }

        return null;
    }

    /**
     * Convenience safety method for retrieving core log level.
     * @return Core log level or current value that the user has set if the native library has not
     * been loaded
     */
    private static int tryGetCoreLogLevel() {
        return (libraryIsLoaded) ? (getCoreLogLevel()) : (level.ordinal());
    }

    /**
     * This is a convenience safety method in the event that the core log level is attempted before
     * initialization.
     * @param level
     */
    private static void trySetCoreLogLevel(int level) {
        if (libraryIsLoaded) {
            setCoreLogLevel(level);
        }
    }
    private static void trySetCoreModuleLogLevel(int module, int level){
        if (libraryIsLoaded) {
            setModuleLevel(module, level);
        }
    }

    private void onNetworkChange() {
        if (initialized && (conversationsClientMap.size() > 0)) {
            refreshRegistrations();
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

    private native boolean initCore(Context context);
    private native void onApplicationForeground();
    private native void onApplicationWakeUp();
    private native void onApplicationBackground();
    private native void destroyCore();
    private native static void setCoreLogLevel(int level);
    private native static void setModuleLevel(int module, int level);
    private native static int getCoreLogLevel();
    private native void refreshRegistrations();
}
