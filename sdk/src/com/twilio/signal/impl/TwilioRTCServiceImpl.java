package com.twilio.signal.impl;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.twilio.signal.TwilioRTCService;
import com.twilio.signal.TwilioRTCService.TwilioBinder;
import com.twilio.signal.impl.logging.Logger;

public class TwilioRTCServiceImpl
{
	private static final Logger logger = Logger.getLogger(TwilioRTCService.class);
	
	private Context context;
	private SignalCore signalCore;
	private Exception callManagerError;
	
	private ConnectivityReceiver connectivityReceiver;
	
	private class ConnectivityReceiver extends BroadcastReceiver
	{

		private static final int RECONNECT_THRESHOLD = 20000;  /* 20 seconds */

		private boolean connected = true;
		private int curNetworkType = -1;
		private int lastConnectedNetworkType = -1;
		private String lastWifiSSID;
		private long lastDisconnectTime = 0;
		
		/*
		 * Sometimes we get this sequence:
		 * (!connected, wasConnected, netChange) -> (connected, !wasConnected, !netChange)
		 * Since we try to ignore quick blips in the network (that is,
		 * a quick change from connected -> disconnected -> connected),
		 * this sequence doesn't result in a reconnect even though we really want
		 * it to since the network type changed.  So we record a "pending" network
		 * change and wait for the network to connect fully before reconnecting.
		 */
		private boolean pendingNetChange;

		public ConnectivityReceiver()
		{
			updateNetworkState();
			if (curNetworkType == -1)
				lastDisconnectTime = System.currentTimeMillis();
		}

		// true if new type is diff from old type
		private boolean updateNetworkState()
		{
			boolean changed = false;

			int oldNetworkType = lastConnectedNetworkType;

			ConnectivityManager connMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connMan.getActiveNetworkInfo();
			if (netInfo != null)
				lastConnectedNetworkType = curNetworkType = netInfo.getType();
			else
				curNetworkType = -1;

			changed = curNetworkType != -1 && lastConnectedNetworkType != oldNetworkType;

			if (curNetworkType == ConnectivityManager.TYPE_WIFI)
			{
				WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiMan.getConnectionInfo();
				if (wifiInfo != null)
				{
					String oldSSID = lastWifiSSID;
					lastWifiSSID = wifiInfo.getSSID();
					if (lastWifiSSID == null || !lastWifiSSID.equals(oldSSID))
						changed = true;
				} else
					lastWifiSSID = null;
				
				logger.d("got wifi SSID: " + lastWifiSSID);
			} else
				lastWifiSSID = null;

			return changed;
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// i'm not entirely sure how this could happen, but we got a report of an
			// NPE in updateNetworkState() while fetching ConnectivityManager.  maybe
			// there's a race where we unset the receiver, set context to null, and then
			// process an onReceive() that was already in the queue before we removed
			// the receiver?  dunno.
			if (context == null)
				return;
			
			boolean haveConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
			logger.d(String.format("got connectivity broadcast: haveConn=%b, reason=%s, isFailover=%b", haveConnection, reason, isFailover));

			boolean wasConnected = connected;
			connected = haveConnection;
			logger.v("old net type: " + curNetworkType);
			boolean netTypeChanged = updateNetworkState();
			logger.v("new net type: " + curNetworkType);
			
			logger.d("NET CHANGE EVENT: connected=" + haveConnection + ", wasConnected=" + wasConnected + ", netTypeChanged=" + netTypeChanged);

			if (wasConnected && !haveConnection)
			{
				// just lost connection, record current time
				lastDisconnectTime = System.currentTimeMillis();
				pendingNetChange = netTypeChanged;
			}
			else if (!wasConnected && haveConnection && !netTypeChanged &&
			         System.currentTimeMillis() - lastDisconnectTime > RECONNECT_THRESHOLD)
			{
				// we got a connection back, and it's on the same type of network as before,
				// but it's been long enough since the disconnect that some connections
				// could have timed out
				needsReconnect();
			}
			else if (netTypeChanged || pendingNetChange)
			{
				// network type has changed, so we almost certainly need to reconnect
				pendingNetChange = false;
				needsReconnect();
			}

			if (netTypeChanged) {
				logger.d("switched networks to type " + curNetworkType);
			}
		}
	}

	public void initialize(Context context)
	{
		if (this.context != null)
			throw new RuntimeException("Service initialize() double-called");
		if (context == null)
			throw new IllegalArgumentException("Context cannot be null");

		this.context = context;

		try
		{
			signalCore = SignalCore.getInstance(context);
		}
		catch (Exception e)
		{
			callManagerError = e;
		}

		connectivityReceiver = new ConnectivityReceiver();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(connectivityReceiver, filter);
	}
	
	public void restoreState(Intent intent, TwilioBinder twBinder)
	{

	}

	public void destroy()
	{
		context.unregisterReceiver(connectivityReceiver);
		connectivityReceiver = null;
		
		SignalCore.destroy();
		signalCore = null;
		callManagerError = null;
		
		context = null;
	}
	
	private void needsReconnect()
	{
		logger.i("Network change; doing reconnect");
		signalCore.onNetworkChanged();
	}

	public SignalCore getSignalCore() {
		return this.signalCore;
	}

}
