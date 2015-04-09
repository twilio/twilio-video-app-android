package com.twilio.signal.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.provider.SyncStateContract.Constants;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.TwilioSignal;
import com.twilio.signal.impl.logging.Logger;

public class TwilioSignalImpl
{
	static final Logger logger = Logger.getLogger(TwilioSignal.class);

	private static volatile TwilioSignalImpl instance;
	private SignalCore signalCore;
	protected Context context;
	protected final Map<UUID, WeakReference<EndpointImpl>> endpoints = new HashMap<UUID, WeakReference<EndpointImpl>>();

	
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
		//TODO - set the lower layer loglevel
	}

	public String getVersion() {
		//TODO - Version.SDK_VERSION;
		return null;
	}
}
