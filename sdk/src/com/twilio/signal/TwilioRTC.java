package com.twilio.signal;

import java.util.Map;

import com.twilio.signal.impl.TwilioSignalImpl;

import android.content.Context;

public class TwilioRTC {

	/**
	 * Interface for the listener object to pass to
	 * {@link TwilioRTC#initialize(Context, InitListener)}.
	 */
	public interface InitListener {
		/**
		 * Callback to report when TwilioRTC Client SDK has been successfully
		 * initialized.
		 */
		public void onInitialized();

		/**
		 * Called if there is an error initializing the TwilioSignal Client SDK.
		 * 
		 * @param error
		 *            An exception describing the error that occurred
		 */
		public void onError(Exception error);
	}

	private TwilioRTC() {}

	/**
	 * Initialize the TwilioSignal Client SDK.
	 * 
	 * @param inContext
	 *            The Application Context from your Android application. Make
	 *            sure you don't pass an Activity Context. You can retrieve the
	 *            Application Context by calling getApplicationContext() on your
	 *            Activity. Cannot be null.
	 * 
	 * @param inListener
	 *            A {@link TwilioRTC.InitListener} that will notify you when the
	 *            service is ready. Cannot be null.
	 * 
	 * @throws IllegalArgumentException
	 */
	public static void initialize(Context inContext,
			TwilioRTC.InitListener inListener) {
		if (inContext == null)
		{
			throw new IllegalArgumentException("Context cannot be null");
		}
		
		if ( inListener == null )
		{
			throw new IllegalArgumentException("Listener cannot be null");
		}
		
		TwilioSignalImpl.getInstance().initialize(inContext, inListener);
	}

	/**
	 * Gets the logging level for messages logged by the TwilioSignal SDK.
	 * 
	 * @return level - The logging level
	 */
	public static int getLogLevel(int level) {
		return TwilioSignalImpl.getInstance().getLogLevel();
	}
	
	
	/**
	 * Sets the logging level for messages logged by the TwilioSignal SDK.
	 * 
	 * @param level - The logging level
	 */
	public static void setLogLevel(int level) {
		TwilioSignalImpl.getInstance().setLogLevel(level);
	}
		
		
	/** 
	 * Create and initialize a new Endpoint object.
	 * 
	 * @param listener - listener object which will receive events from a Endpoint object.
	 *
	 * @return The initialized Endpoint object, or null if the SDK was not initialized
	 */
	public static Endpoint createEndpoint(String token, EndpointListener listener)
	{
		return null;//TwilioSignalImpl.getInstance().createEndpoint(token, listener);
	}
	
	/** 
	 * Create and initialize a new Endpoint object.
	 * 
	 * @param listener - listener object which will receive events from a Endpoint object.
	 *
	 * @return The initialized Endpoint object, or null if the SDK was not initialized
	 */
	public static Endpoint createEndpoint(String token, Map<String, String> options, EndpointListener listener)
	{
		return TwilioSignalImpl.getInstance().createEndpoint(options, listener);
	}
	
	
	/**
	 * Returns the version of the TwilioSignal SDK.
	 * 
	 *@return The version of the SDK.
	 */
	public static String getVersion()
	{
		return TwilioSignalImpl.getInstance().getVersion();
	}


}
