package com.twilio.signal.impl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;

import com.twilio.common.TwilioAccessManager;
import com.twilio.signal.ConversationsClientListener;
import com.twilio.signal.TwilioRTC;
import com.twilio.signal.TwilioRTC.LogLevel;
import com.twilio.signal.TwilioRTCService;
import com.twilio.signal.TwilioRTCService.TwilioBinder;
import com.twilio.signal.impl.logging.Logger;


public class TwilioRTCImpl {

	static {
		System.loadLibrary("twilio-native");
	}

	static final Logger logger = Logger.getLogger(TwilioRTCImpl.class);

	private static final String TWILIO_SIGNAL_SERVICE_NAME = "com.twilio.signal.TwilioRTCService";

	private static volatile TwilioRTCImpl instance;
	private static int level = 0;
	protected Context context;
	private boolean sdkInited;
	private boolean sdkIniting;
	private ServiceConnection serviceConn;

	protected TwilioBinder twBinder;

	protected final Map<UUID, WeakReference<ConversationsClientImpl>> conversationsClientMap = new HashMap<UUID, WeakReference<ConversationsClientImpl>>();

	private static final String[] requiredPermissions = {
		"android.permission.INTERNET",
		"android.permission.RECORD_AUDIO",
		"android.permission.MODIFY_AUDIO_SETTINGS",
		"android.permission.ACCESS_NETWORK_STATE",
		"android.permission.ACCESS_WIFI_STATE",
	};

	public static TwilioRTCImpl getInstance() {
		if (instance == null) {
			synchronized (TwilioRTCImpl.class) {
				if (instance == null)
					instance = new TwilioRTCImpl();
			}
		}

		if(level != 0) {
			// Re-apply the log level. Initialization sets a default log level.
			setLogLevel(level);
		}

		return instance;
	}

	public Context getContext() {
		return context;
	}

	public static void setInstance(TwilioRTCImpl instance) {
		TwilioRTCImpl.instance = instance;
	}

	TwilioRTCImpl() {}

	public void initialize(Context inContext, final TwilioRTC.InitListener inListener) {

		if (isInitialized() || isInitializing())
		{
			inListener.onError(new RuntimeException("Twilio.initialize() already called"));
			return;
		}

		sdkIniting = true;

		try {
			boolean success = initCore(inContext);
			if(!success) {
				throw new RuntimeException("Twilio failed to initialize");
			}
			PackageManager pm = inContext.getPackageManager();
			PackageInfo pinfo = pm.getPackageInfo(inContext.getPackageName(),
					PackageManager.GET_PERMISSIONS
							| PackageManager.GET_SERVICES);

			// check application permissions
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

			// check that the service is declared properly
			boolean serviceFound = false;
			if (pinfo.services != null)
			{
				for (ServiceInfo service : pinfo.services)
				{
					if (service.name.equals(TWILIO_SIGNAL_SERVICE_NAME))
					{
						serviceFound = true;
						if (service.exported)
							throw new RuntimeException(TWILIO_SIGNAL_SERVICE_NAME+" is exported.  You must add android:exported=\"false\" to the <service> declaration in AndroidManifest.xml");
					}
				}
			}

			if (!serviceFound)
				throw new RuntimeException(TWILIO_SIGNAL_SERVICE_NAME + " is not declared in AndroidManifest.xml");
		} catch (Exception e) {
			inListener.onError(e);
			sdkIniting = false;
			return;
		}


		context = inContext;
		final Intent service = new Intent(context, TwilioRTCService.class);

		serviceConn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				sdkIniting = false;
				sdkInited = true;

				// we must never die!
				context.startService(service);
				twBinder = (TwilioBinder)binder;
				TwilioRTCImpl twilioRtc = twBinder.getTwiloRtc();
				//signalCore = twBinder.getSignalCore();
				//if (signalCore != null)
				if (twilioRtc != null)
				{
					inListener.onInitialized();
				} else {
					onServiceDisconnected(name);
					inListener.onError(null);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				sdkIniting = sdkInited = false;
				twBinder = null;
				context = null;
			}

		};

		// We need to both startService() and bindService() here.  The startService() call
		// will ensure that the Service keeps running even if the calling Activity gets
		// destroyed and recreated.  The bindService() gives us the IBinder instance.

		if (!context.bindService(service, serviceConn, Context.BIND_AUTO_CREATE)) {
			context = null;
			inListener.onError(new RuntimeException("Failed to start TwiloRTCService.  Please ensure it is declared in AndroidManifest.xml"));
		}

	}

	public ConversationsClientImpl createConversationsClient(TwilioAccessManager accessManager, Map<String, String> options, ConversationsClientListener inListener) {
		if(options != null && accessManager != null) {
			final ConversationsClientImpl conversationsClient = new ConversationsClientImpl(context, accessManager, inListener);
			long nativeEndpointObserverHandle = conversationsClient.getEndpointObserverHandle();
			if (nativeEndpointObserverHandle == 0) {
				return null;
			}
			final long nativeEndpointHandle = createEndpoint(accessManager, nativeEndpointObserverHandle);
			if (nativeEndpointHandle == 0) {
				return null;
			}
			conversationsClient.setNativeHandle(nativeEndpointHandle);
 			synchronized (conversationsClientMap)
			{
				conversationsClientMap.put(conversationsClient.getUuid(), new WeakReference<ConversationsClientImpl>(conversationsClient));
			}
			return conversationsClient;
		}
		return null;
	}

	public static void setLogLevel(int level) {
		boolean validLevel = true;
		/*
		 * The Twilio RTC Log Levels are defined differently in the Twilio Logger
		 * which is based off android.util.Log.
		 */
		switch(level) {
			case LogLevel.DISABLED:
				Logger.setLogLevel(Log.ASSERT);
				break;
			case LogLevel.ERROR:
				Logger.setLogLevel(Log.ERROR);
				break;
			case LogLevel.WARNING:
				Logger.setLogLevel(Log.WARN);
				break;
			case LogLevel.INFO:
				Logger.setLogLevel(Log.INFO);
				break;
			case LogLevel.DEBUG:
				Logger.setLogLevel(Log.DEBUG);
				break;
			case LogLevel.VERBOSE:
				Logger.setLogLevel(Log.VERBOSE);
				break;
			default:
				// Set the log level to assert/disabled if the value passed in is unknown
				Logger.setLogLevel(Log.ASSERT);
				level = TwilioRTC.LogLevel.DISABLED;
				break;
		}
		setCoreLogLevel(level);
		// Save the log level
		TwilioRTCImpl.level = level;
	}

	public static int getLogLevel() {
		return getCoreLogLevel();
	}

	public boolean isInitialized() {
		return sdkInited;
	}

	boolean isInitializing() {
		return sdkIniting;
	}

	private void updateServiceState() {
		if (context == null || twBinder == null)
			return;

		Intent intent = new Intent(context, TwilioRTCService.class);
		context.startService(intent);
	}

	public ConversationsClientImpl findDeviceByUUID(UUID uuid) {
		synchronized (conversationsClientMap)
		{
			WeakReference<ConversationsClientImpl> deviceRef = conversationsClientMap.get(uuid);
			if (deviceRef != null) {
				ConversationsClientImpl device = deviceRef.get();
				if (device != null) {
					return device;
				} else {
					conversationsClientMap.remove(uuid);
				}
			}
		}
        	return null;
	}

	private native boolean initCore(Context context);
	private native long createEndpoint(TwilioAccessManager accessManager, long nativeEndpointObserver);
	private native static void setCoreLogLevel(int level);
	private native static int getCoreLogLevel();

}
