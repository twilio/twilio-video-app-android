package com.twilio.signal.impl;

public class TwilioCore {
	
	static
	{
		System.loadLibrary("twilio-native");
	}
	
	private native String initCore();
	private static native boolean isCoreInitialized();
	private static TwilioCore instance;
	
	public static TwilioCore getInstance() {
		if (instance == null) {
			instance = new TwilioCore();
		}
		return instance;
	}
	
	public TwilioCore() {
		
	}
	
	public void initSignalCore() {
		initCore();
	}
	
	public static boolean isSignalCoreInitialized() {
		return isCoreInitialized();
	}
}