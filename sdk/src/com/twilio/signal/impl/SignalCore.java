package com.twilio.signal.impl;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

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
	private Object callCommandHandler;
	
	private native boolean initCore();
	private native boolean isCoreInitialized();
	private native boolean setLogLevel();
	private native void registerEndpoint(Endpoint endopoint);
	private native boolean login(CredentialInfo[] creadInfo, SignalCoreConfig config, Endpoint endpoint);
	private native boolean logout(Endpoint endpoint);
	
	public static SignalCore getInstance() {
		if (singleton == null) {
			synchronized (singletonLock) {
				if (singleton == null) {
					singleton = new SignalCore();
					singleton.initSignalCore();
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
		
		//login(credInfoArray, signalCoreCfg, endpoint);
		
		if (this.callCommandHandler == null) {
			this.callCommandHandler = new CallCommandHandlerImpl(credInfoArray, signalCoreCfg, endpoint);
		}
		
		return endpoint;
	}
	
	public boolean register() {
		login(null, null, null);
		return true;
	}
	
	public boolean unregister(Endpoint endpoint) {
		logout(endpoint);
		return true;
	}
	
	
	public void onNetworkChanged() {
		// TODO Auto-generated method stub
		
	}
	public static void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	
	class CallCommandHandlerImpl extends Thread implements CallCommandHandler {
		

		Handler callHandler = null;
		Looper looper = null;
		CredentialInfo[] credInfoArray;
		SignalCoreConfig signalCoreCfg;
		Endpoint endpoint;
		

		public CallCommandHandlerImpl(CredentialInfo[] credInfo,SignalCoreConfig config, Endpoint endpoint) {
			// just call start immediately
			this.credInfoArray = credInfo;
			this.signalCoreCfg = config;
			this.endpoint = endpoint;
			this.start();
		}

		public void postCommand(Runnable command) {
			if (callHandler != null) {
				callHandler.post(command);
			}
		}

		public void run() {
			try {
				login(credInfoArray, signalCoreCfg, endpoint);
				Looper.prepare(); // bind to this thread
				looper = Looper.myLooper();

				callHandler = new Handler(); // bind the handler to the looper for
												// this thread

				Looper.loop(); // run until we're shut down
			} catch (Throwable t) {
				
			} finally {
				
			}
		}

		public void destroy() {
			if (looper != null)
				looper.quit();

			this.interrupt(); // TODO: is this necessary?
		}
	}
	
	
	interface CallCommandHandler {
		public void destroy();
	}
	
	
	
}