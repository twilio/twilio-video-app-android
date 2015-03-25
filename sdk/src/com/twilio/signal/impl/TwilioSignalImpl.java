package com.twilio.signal.impl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.TwilioSignal;
import com.twilio.signal.impl.logging.Logger;

public class TwilioSignalImpl
{
	static final Logger logger = Logger.getLogger(TwilioSignal.class);

	private static volatile TwilioSignalImpl instance;
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
	
	TwilioSignalImpl() {}
	
	public void initialize(Context inContext, final TwilioSignal.InitListener inListener)
	{
		context = inContext;
		TwilioCore test = TwilioCore.getInstance();
		if (!test.isSignalCoreInitialized()) {
			test.initSignalCore();
		} else {
			inListener.onInitialized();
		}
	}

	public Endpoint createEndpoint(String inCapabilityToken, EndpointListener inListener)
	{
		if (!TwilioCore.isSignalCoreInitialized())
		{
			logger.e("Twilio.createDevice() called without a successful call to Twilio.initialize()");
			return null;
		}

		final EndpointImpl endpoint = new EndpointImpl(this, inCapabilityToken, inListener);
		synchronized (endpoints)
		{
			endpoints.put(endpoint.getUuid(), new WeakReference<EndpointImpl>(endpoint));
		}
		
		return endpoint;
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
