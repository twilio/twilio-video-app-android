package com.twilio.signal;

public class Test {
	
	static
	{
		System.loadLibrary("twilio-native");
	}
	
	
	private native String initCore();
	private native boolean isCoreInitialized();
	private native boolean login();
	
	private static Test instance;
	
	public static Test getInstance() {
		if (instance == null) {
			instance = new Test();
		}
		return instance;
	}
	
	public Test() {
		
	}
	
	public void initSignalCore() {
		initCore();
	}
	
	public boolean isSignalCoreInitialized() {
		return isCoreInitialized();
	}
	
	public boolean loginUser() {
		return login();
	}
}