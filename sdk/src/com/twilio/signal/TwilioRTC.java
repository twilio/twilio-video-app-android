package com.twilio.signal;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.signal.impl.TwilioRTCImpl;

public class TwilioRTC {

	/**
	 * Interface for the listener object to pass to
	 * {@link TwilioRTC#initialize(Context, InitListener)}.
	 */
	public interface InitListener {
		/**
		 * Callback to report when Twilio RTC Conversations Client
		 * has been successfully initialized.
		 */
		public void onInitialized();

		/**
		 * Called if there is an error initializing the Twilio RTC
		 * Conversations Client.
		 * 
		 * @param exception An exception describing the error that occurred
		 */
		public void onError(Exception exception);
	}

	/**
	 * Log levels for the Twilio RTC Conversations Client
	 */
	public final class LogLevel {
		public static final int DISABLED = 0;
		public static final int ERROR = 3;
		public static final int WARNING= 4;
		public static final int INFO = 6;
		public static final int DEBUG = 7;
		public static final int VERBOSE = 8;
	}

	private TwilioRTC() {}

	/**
	 * Initialize the Twilio RTC Conversations Client.
	 * 
	 * @param applicationContext
	 *            The application context of your Android application
	 * 
	 * @param initListener
	 *            A {@link TwilioRTC.InitListener} that will notify you
	 *            when the service is ready
	 * 
	 * @throws NullPointerException
	 */
	public static void initialize(Context applicationContext,
			TwilioRTC.InitListener initListener) {
		if (applicationContext == null) {
			throw new NullPointerException("applicationContext must not be null");
		}
		if (initListener == null) {
			throw new NullPointerException("initListener must not be null");
		}

		TwilioRTCImpl.getInstance().initialize(applicationContext, initListener);
	}

	/**
	 * Gets the logging level for messages logged by the Twilio RTC Conversations SDK.
	 * 
	 * @return the logging level
	 */
	public static int getLogLevel() {
		return TwilioRTCImpl.getLogLevel();
	}

	/**
	 * Sets the logging level for messages logged by the Twilio RTC Conversations SDK.
	 * 
	 * @param level - the logging level
	 */
	public static void setLogLevel(int level) {
		TwilioRTCImpl.setLogLevel(level);
	}

	/**
	 * Creates a new ConversationsClient.
	 * 
	 * @param token - Access token
	 * @param listener - a listener that receive events from the ConversationsClient.
	 *
	 * @return the initialized ConversationsClient, or null if the Twilio RTC Conversations Client
         *         was not initialized
	 */
	public static ConversationsClient createConversationsClient(String token, ConversationsClientListener listener) {
		if (token == null) {
			throw new NullPointerException("token must not be null");
		}
		if (listener == null) {
			throw new NullPointerException("listener must not be null");
		}
		TwilioAccessManager manager = TwilioAccessManagerFactory.createAccessManager(token, null);
		Map<String, String> options = new HashMap<String, String>();
		return TwilioRTCImpl.getInstance().createConversationsClient(manager, options, listener);
	}
	
	/**
	 * Creates a new ConversationsClient.
	 * 
	 * @param accessManager - instance of TwilioAccessManager that is handling token lifetime
	 * @param listener - a listener that receive events from the ConversationsClient.
	 *
	 * @return the initialized ConversationsClient, or null if the Twilio RTC Conversations Client
         *         was not initialized
	 */
	public static ConversationsClient createConversationsClient(TwilioAccessManager accessManager, ConversationsClientListener listener) {
		if (accessManager == null) {
			throw new NullPointerException("access manager must not be null");
		}
		if (listener == null) {
			throw new NullPointerException("listener must not be null");
		}
		Map<String, String> options = new HashMap<String, String>();
		return TwilioRTCImpl.getInstance().createConversationsClient(accessManager, options, listener);
	}
	
	/**
	 * Creates a new ConversationsClient.
	 * 
	 * @param accessManager - instance of TwilioAccessManager that is handling token lifetime
	 * @param options - options map <key, value>
	 * @param listener - a listener that receive events from the ConversationsClient.
	 *
	 * @return the initialized ConversationsClient, or null if the Twilio RTC Conversations Client
         *         was not initialized
	 */
	public static ConversationsClient createConversationsClient(TwilioAccessManager accessManager, Map<String, String> options, ConversationsClientListener listener) {
		if (accessManager == null) {
			throw new NullPointerException("access manager must not be null");
		}
		if (options == null) {
			throw new NullPointerException("options must not be null");
		}
		if (listener == null) {
			throw new NullPointerException("listener must not be null");
		}

		return TwilioRTCImpl.getInstance().createConversationsClient(accessManager, options, listener);
	}

	/**
	 * Returns the version of the Twilio RTC Conversations SDK.
	 * 
	 * @return the version of the SDK
	 */
	public static String getVersion() {
		return Version.SDK_VERSION;
	}

}
