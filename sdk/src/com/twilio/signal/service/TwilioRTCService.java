package com.twilio.signal.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.twilio.signal.impl.SignalCore;
import com.twilio.signal.impl.TwilioSignalServiceImpl;
import com.twilio.signal.impl.logging.Logger;


public class TwilioRTCService extends Service
{
	private static final Logger logger = Logger.getLogger(TwilioRTCService.class);
	
	private TwilioSignalServiceImpl serviceImpl;
	
	public class TwilioBinder extends Binder
	{
		
		public SignalCore getSignalCore()
		{
			return serviceImpl.getSignalCore();
		}
	}
	
	private final TwilioBinder binder = new TwilioBinder();
	private int lastStartId = -1;
	
	@Override
	public void onCreate()
	{
		serviceImpl = new TwilioSignalServiceImpl();
		serviceImpl.initialize(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// if we get here, someone has called startService() and passed
		// us an intent that might have some extra data in it that lists
		// active Devices.  if the SDK is *not* initialized at this point,
		// it means that the OS killed us and is now restarting us, so we
		// should re-initialize the SDK from within and set up the Device
		// list as best we can.
		
		if ((flags & Service.START_FLAG_REDELIVERY) != 0)
			serviceImpl.restoreState(intent, binder);
		
		// we only want the most recent intent to be re-delivered, so
		// 'cancel' any previous ones.
		if (lastStartId != -1)
			stopSelfResult(lastStartId);
		lastStartId = startId;
		
		// returning START_REDELIVER_INTENT will cause Android to re-deliver
		// the intent we just got if it needs to restart us for any reason.
		// this means we don't even have to persist our own state somewhere,
		// as the OS will do it for us.
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		serviceImpl.destroy();
		serviceImpl = null;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}
}
