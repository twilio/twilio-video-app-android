package com.twilio.signal.impl;

import android.annotation.SuppressLint;

import com.twilio.signal.Endpoint;

public class SignalCore implements SignalCoreCallBacks {
	
	static
	{
		System.loadLibrary("twilio-native");
	}
	
	private native boolean initCore();
	private native boolean isCoreInitialized();
	private native boolean setLogLevel();
	private native void registerEndpoint(Endpoint endopoint);
	private native boolean login();
	private static SignalCore instance;
	
	public static SignalCore getInstance() {
		if (instance == null) {
			instance = new SignalCore();
		}
		return instance;
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
	
	public Endpoint createEndpoint() {
		return null;
	}
	
	public boolean register() {
		login();
		return true;
	}

	@Override
	public void onRegistrationComplete() {
		// TODO Auto-generated method stub
		
	}
	
}