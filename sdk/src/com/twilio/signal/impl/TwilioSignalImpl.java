package com.twilio.signal.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

import com.twilio.signal.EndpointListener;
import com.twilio.signal.TwilioSignalService;
import com.twilio.signal.TwilioSignalService.TwilioBinder;
import com.twilio.signal.TwilioSignal;
import com.twilio.signal.impl.logging.Logger;

public class TwilioSignalImpl
{
	static final Logger logger = Logger.getLogger(TwilioSignal.class);
	
	private static final String TWILIO_SIGNAL_SERVICE_NAME = "com.twilio.signal.TwilioSignalService";

	private static volatile TwilioSignalImpl instance;
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
	
	public static TwilioSignalImpl getInstance()
	{
		if (instance == null)
		{
			synchronized (TwilioSignalImpl.class)
			{
				if (instance == null)
					instance = new TwilioSignalImpl();
			}
		}
		
		return instance;
	}
	
	public Context getContext() {
		return context;
	}

	public static void setInstance(TwilioSignalImpl instance) {
		TwilioSignalImpl.instance = instance;
	}

	TwilioSignalImpl() {}
	
	public void initialize(Context inContext, final TwilioSignal.InitListener inListener)
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
							throw new RuntimeException("TwilioClientService is exported.  You must add android:exported=\"false\" to the <service> declaration in AndroidManifest.xml");
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
		final Intent service = new Intent(context, TwilioSignalService.class);

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
				}
				else
				{
					Exception error = twBinder.getError();
					onServiceDisconnected(name);  // nulls out twBinder
					inListener.onError(error);
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
			inListener.onError(new RuntimeException("Failed to start TwilioClientService.  Please ensure it is declared in AndroidManifest.xml"));
		}
	
		this.context = inContext;
		this.signalCore = SignalCore.getInstance();
		if (!signalCore.isSignalCoreInitialized()) {
			boolean ret = signalCore.initSignalCore();
			if(ret) {
				inListener.onInitialized();
			} else {
				inListener.onError(null);
			}
		} else {
			inListener.onInitialized();
		}
		
	}

	public EndpointImpl createEndpoint(Map<String, String> options, String inCapabilityToken, EndpointListener inListener)
	{
		if(options != null) {
			String authToken = options.get(TwilioConstants.EndpointOptionCapabilityTokenKey);
			String sURL = options.get(TwilioConstants.EndpointOptionStunURLKey);
			String tURL = options.get(TwilioConstants.EndpointOptionTurnURLKey);
			String userName = options.get(TwilioConstants.EndpointOptionUserNameKey);
			String password = options.get(TwilioConstants.EndpointOptionPasswordKey);
			
			final List<CredentialInfo> credentialInfo = new ArrayList<CredentialInfo>(1);
			credentialInfo.add(new CredentialInfo(authToken, sURL, tURL, userName, password, SignalCore.getInstance()));

			final EndpointImpl endpoint  = this.signalCore.createEndpoint(credentialInfo, inListener);
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
		
		Intent intent = new Intent(context, TwilioSignalService.class);
		context.startService(intent);
	}
	
	
}
