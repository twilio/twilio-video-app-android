package com.twilio.signal.impl;

import android.content.Context;
import android.content.ServiceConnection;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Test;
import com.twilio.signal.TwilioSignal;
import com.twilio.signal.impl.logging.Logger;

public class TwilioSignalImpl
{
	static final Logger logger = Logger.getLogger(TwilioSignal.class);

	private static volatile TwilioSignalImpl instance;
	
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
	
	// application context from which we can obtain system services
	protected Context context;
	private ServiceConnection serviceConn;

	
	TwilioSignalImpl()
	{

	}
	
	public void initialize(Context inContext, final TwilioSignal.InitListener inListener)
	{
		context = inContext;
		Test test = Test.getInstance();
		if (!test.isSignalCoreInitialized()) {
			test.initSignalCore();
		} else {
			inListener.onInitialized();
		}
	}

	public Endpoint createEndpoint(String inCapabilityToken, EndpointListener inListener)
	{
		return null;
	}

	public void setLogLevel(int level)
	{
		Logger.setLogLevel(level);
		//TODO - set the lower layer loglevel
	}

	public String getVersion() {
		
		return null;//Version.SDK_VERSION;
	}
}
