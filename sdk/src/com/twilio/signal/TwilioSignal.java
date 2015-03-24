package com.twilio.signal;


import android.content.Context;

public class TwilioSignal {

	/**
	 * Interface for the listener object to pass to
	 * {@link TwilioSignal#initialize(Context, InitListener)}.
	 */
	public interface InitListener {
		/**
		 * Callback to report when TwilioSignal Client SDK has been successfully
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

	private TwilioSignal() {}

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
	 *            A {@link TwilioSignal.InitListener} that will notify you when the
	 *            service is ready. Cannot be null.
	 * 
	 * @throws IllegalArgumentException
	 */
	public static void initialize(Context inContext,
			TwilioSignal.InitListener inListener) {

	}

	/**
	 * Sets the logging level for messages logged by the TwilioSignal SDK.
	 * 
	 * @param level - The logging level
	 */
	public static void setLogLevel(int level) {

	}
	
	
	/** 
	 * Create and initialize a new Endpoint object.
	 * 
	 * @param listener - listener object which will receive events from a Endpoint object.
	 *
	 * @return The initialized Endpoint object, or null if the SDK was not initialized
	 */
	public static Endpoint createEndpoint(String inCapabilityToken, EndpointListener listener)
	{
		return null;
	}


}
