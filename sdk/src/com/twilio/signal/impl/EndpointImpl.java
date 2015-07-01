package com.twilio.signal.impl;

import java.util.Set;
import java.util.UUID;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Media;
import com.twilio.signal.impl.logging.Logger;

public class EndpointImpl implements Endpoint, NativeHandleInterface, Parcelable{

	static final Logger logger = Logger.getLogger(EndpointImpl.class);
	
	class EndpointObserverInternal implements NativeHandleInterface {
		
		private long nativeEndpointObserver;
		
		public EndpointObserverInternal(EndpointListener listener) {
			//this.listener = listener;
			this.nativeEndpointObserver = wrapNativeObserver(listener);
		}
		
		private native long wrapNativeObserver(EndpointListener listener);
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeEndpointObserver);

		@Override
		public long getNativeHandle() {
			return nativeEndpointObserver;
		}
		
	}

	private final UUID uuid = UUID.randomUUID();
	private Context context;
	private EndpointListener listener;
	private String userName;
	private PendingIntent incomingIntent = null;
	private EndpointObserverInternal endpointObserver;
	private long nativeEndpointHandle;
	
	
	public UUID getUuid() {
		return uuid;
	}


	@Override
	public int hashCode() {
		return super.hashCode();
	}

/*
	public EndpointImpl(Context context,
						EndpointListener inListener,
						long nativeEndpointHandle) {
		this.context = context;
		this.listener = inListener;
		this.nativeEndpointHandle = nativeEndpointHandle;
	}
*/

	EndpointImpl(Context context,
			EndpointListener inListener) {
		this.context = context;
		this.listener = inListener;
		//this.nativeEndpointHandle = nativeEndpointHandle;
		this.endpointObserver = new EndpointObserverInternal(inListener);
	}
	
	void setNativeHandle(long nativeEndpointHandle) {
		this.nativeEndpointHandle = nativeEndpointHandle;
	}
	
	long getEndpointObserverHandle() {
		return this.endpointObserver.getNativeHandle();
	}


	@Override
	public void listen() {
		//SignalCore.getInstance(this.context).register();
		if (nativeEndpointHandle != 0) {
			listen(nativeEndpointHandle);
		}
	}


	@Override
	public void unlisten() {
		SignalCore.getInstance(this.context).unregister(this);
	}




	@Override
	public void setEndpointListener(EndpointListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isListening() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Conversation createConversation(Set<String> participants,
			Media localMedia, ConversationListener listener) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override /* Parcelable */
	public int describeContents()
	{
        return 0;
    }

	@Override /* Parcelable */
    public void writeToParcel(Parcel out, int flags)
	{
        out.writeSerializable(uuid);
    }

	/* Parcelable */
    public static final Parcelable.Creator<EndpointImpl> CREATOR = new Parcelable.Creator<EndpointImpl>()
    {
    	@Override
        public EndpointImpl createFromParcel(Parcel in)
        {
            UUID uuid = (UUID)in.readSerializable();
            TwilioRTCImpl twImpl = TwilioRTCImpl.getInstance();
            return twImpl.findDeviceByUUID(uuid);
        }

    	@Override
        public EndpointImpl[] newArray(int size)
        {
            throw new UnsupportedOperationException();
        }
    };


	public void onIncomingInvite() {
		logger.d("Received Incoming notification");
		if (incomingIntent != null) {
			logger.d("Received Incoming notification, calling intent");
			Intent intent = new Intent();
			intent.putExtra(Endpoint.EXTRA_DEVICE, this);
			if (intent.hasExtra(Endpoint.EXTRA_DEVICE)) {
				logger.d("Received Incoming notification, calling intent has extra");
			} else {
				logger.d("Received Incoming notification, calling intent do not have extra");
			}
			try {
				incomingIntent.send(context, 0, intent);
			} catch (final CanceledException e) {

			}
		}
	}


	@Override
	public long getNativeHandle() {
		return nativeEndpointHandle;
	}

	//Native implementation
	private native void listen(long nativeEndpoint);


}
