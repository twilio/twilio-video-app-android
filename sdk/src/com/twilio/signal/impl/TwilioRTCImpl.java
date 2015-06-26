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

import com.twilio.signal.EndpointListener;
import com.twilio.signal.TwilioRTC;
import com.twilio.signal.TwilioRTCService;
import com.twilio.signal.TwilioRTCService.TwilioBinder;
import com.twilio.signal.impl.logging.Logger;

public class TwilioRTCImpl
{

	static
	{
		System.loadLibrary("twilio-native");
	}

	static final Logger logger = Logger.getLogger(TwilioRTCImpl.class);

	private static final String TWILIO_SIGNAL_SERVICE_NAME = "com.twilio.signal.TwilioRTCService";

	private static volatile TwilioRTCImpl instance;
	private SignalCore signalCore;
	protected Context context;
	private boolean sdkInited;
	private boolean sdkIniting;
	private ServiceConnection serviceConn;
	protected TwilioBinder twBinder;

	protected final Map<UUID, WeakReference<EndpointImpl>> endpoints = new HashMap<UUID, WeakReference<EndpointImpl>>();

	private static final String[] requiredPermissions = {
		"android.permission.INTERNET",
		"android.permission.RECORD_AUDIO",
		"android.permission.MODIFY_AUDIO_SETTINGS",
		"android.permission.ACCESS_NETWORK_STATE",
		"android.permission.ACCESS_WIFI_STATE",
	};

	public static TwilioRTCImpl getInstance()
	{
		if (instance == null)
		{
			synchronized (TwilioRTCImpl.class)
			{
				if (instance == null)
					instance = new TwilioRTCImpl();
			}
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

	public void initialize(Context inContext, final TwilioRTC.InitListener inListener)
	{

		if (isInitialized() || isInitializing())
		{
			inListener.onError(new RuntimeException("Twilio.initialize() already called"));
			return;
		}

		sdkIniting = true;

		try {
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

		serviceConn = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder binder)
			{
				sdkIniting = false;
				sdkInited = true;

				// we must never die!
				context.startService(service);
				twBinder = (TwilioBinder)binder;
				signalCore = twBinder.getSignalCore();
				if (signalCore != null)
				{
					inListener.onInitialized();
				} else {
					onServiceDisconnected(name);
					inListener.onError(null);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				sdkIniting = sdkInited = false;
				twBinder = null;
				context = null;
			}
		};

		// We need to both startService() and bindService() here.  The startService() call
		// will ensure that the Service keeps running even if the calling Activity gets
		// destroyed and recreated.  The bindService() gives us the IBinder instance.

		if (!context.bindService(service, serviceConn, Context.BIND_AUTO_CREATE))
		{
			context = null;
			inListener.onError(new RuntimeException("Failed to start TwiloRTCService.  Please ensure it is declared in AndroidManifest.xml"));
		}

	}

	public EndpointImpl createEndpoint(String token, Map<String, String> options, EndpointListener inListener)
	{
		if(options != null && token != null) {
			Log.d("!nn!", "Token:"+token);
			EndpointListenerInternal listener = new EndpointListenerInternal(inListener);
			final long nativeEndpointHandle = createEndpoint(token, listener.getNativeHandle());
			final EndpointImpl endpoint = new EndpointImpl(context, listener, nativeEndpointHandle);
			synchronized (endpoints)
			{
				endpoints.put(endpoint.getUuid(), new WeakReference<EndpointImpl>(endpoint));
			}
			return endpoint;
		}
		return null;
	}

	public void setLogLevel(int level)
	{
		Logger.setLogLevel(level);
	}

	public String getVersion()
	{
		return  null;//Version.SDK_VERSION;;
	}

	public boolean isInitialized()
	{
		return sdkInited;
	}

	boolean isInitializing()
	{
		return sdkIniting;
	}

	private void updateServiceState()
	{
		if (context == null || twBinder == null)
			return;

		Intent intent = new Intent(context, TwilioRTCService.class);
		context.startService(intent);
	}


	EndpointImpl findDeviceByUUID(UUID uuid)
	{
        synchronized (endpoints)
        {
        	WeakReference<EndpointImpl> deviceRef = endpoints.get(uuid);
        	if (deviceRef != null) {
        		EndpointImpl device = deviceRef.get();
        		if (device != null)
        			return device;
        		else
        			endpoints.remove(uuid);
        	}
        }

        return null;
	}

	public int getLogLevel() {
		// TODO Auto-generated method stub
		return Logger.getLogLevel();
	}

	//native methods
	private native boolean initCore(Context context);
	private native long createEndpoint(String token, long nativeEndpointObserver);


}
