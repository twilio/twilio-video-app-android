package com.twilio.signal.impl;

import java.util.List;

import android.annotation.SuppressLint;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;

public class SignalCore {
	
	static
	{
		System.loadLibrary("twilio-native");
	}
	
	private static final Object singletonLock = new Object();
	private static SignalCore singleton;
	private SignalCoreConfig.Callbacks callbacks;
	
	private native boolean initCore();
	private native boolean isCoreInitialized();
	private native boolean setLogLevel();
	private native void registerEndpoint(Endpoint endopoint);
	private native boolean login(CredentialInfo[] creadInfo, SignalCoreConfig config);
	private native boolean logout(String userName);
	
	public static SignalCore getInstance() {
		if (singleton == null) {
			synchronized (singletonLock) {
				if (singleton == null) {
					singleton = new SignalCore();
				}
			}
		}	
		return singleton;
	}
	
	public SignalCore() {
		
	}
	
	@SuppressLint("NewApi")
	public boolean initSignalCore() {
		return initCore();
	}
	
	public boolean isSignalCoreInitialized() {
		return isCoreInitialized();
	}
	
	public EndpointImpl createEndpoint(List<CredentialInfo> credInfo, EndpointListener inListener) {
		
		CredentialInfo[] credInfoArray = new CredentialInfo[credInfo != null ? credInfo.size() : 0];
		if (credInfoArray != null) {
			int nCreds = credInfo.size();
			for (int i = 0; i < nCreds; ++i)
				credInfoArray[i] = credInfo.get(i);
		}
		
		EndpointImpl endpoint = new EndpointImpl(TwilioSignalImpl.getInstance(), credInfo.get(0).getCapabilityToken(), inListener);
		endpoint.setUserName(credInfo.get(0).getUserName());
		SignalCoreConfig signalCoreCfg = new SignalCoreConfig(endpoint);
		
		login(credInfoArray, signalCoreCfg);
		return endpoint;
	}
	
	public boolean register() {
		login(null, null);
		return true;
	}
	
	public boolean unregister(String userName) {
		logout(userName);
		return true;
	}
	
}