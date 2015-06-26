package com.twilio.signal.impl;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;

public class SignalCore {
	
	/*static
	{
		System.loadLibrary("twilio-native");
	} */
	
	private static final Object singletonLock = new Object();
	private static SignalCore singleton;
	private Object callCommandHandler;
	
	private native boolean initCore(Context context);
	private native boolean isCoreInitialized();
	private native boolean setLogLevel();
	//private native void registerEndpoint(Endpoint endopoint);
	private native boolean login(CredentialInfo[] creadInfo, SignalCoreConfig config, Endpoint endpoint);
	private native boolean logout(Endpoint endpoint);
	private native boolean acceptNative(Endpoint endpoint);
	
	public static SignalCore getInstance(Context context) {
		if (singleton == null) {
			synchronized (singletonLock) {
				if (singleton == null) {
					singleton = new SignalCore(context);
				}
			}
		}	
		return singleton;
	}
	
	public SignalCore(Context context) {
		initCore(context);
	}

	public boolean isSignalCoreInitialized() {
		return isCoreInitialized();
	}
	
	public boolean register() {
		login(null, null, null);
		return true;
	}
	
	public boolean unregister(Endpoint endpoint) {
		logout(endpoint);
		return true;
	}
	
	public boolean accept(Endpoint endpoint) {
		acceptNative(endpoint);
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