package com.twilio.signal.impl;

import java.util.Map;
import java.util.UUID;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.twilio.signal.Capability;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.impl.logging.Logger;

public class EndpointImpl implements Endpoint, SignalCoreConfig.Callbacks, Parcelable{
	
	static final Logger logger = Logger.getLogger(EndpointImpl.class);

	
	private final UUID uuid = UUID.randomUUID();
	private SignalCore sigalCore;
	private Context context;
	private EndpointListener listener;
	private String userName;
	private PendingIntent incomingIntent = null;

	
	private native Endpoint createEndpoint();


	public UUID getUuid() {
		return uuid;
	}


	@Override
	public int hashCode() {
		return super.hashCode();
	}


	public EndpointImpl(TwilioSignalImpl twilioSignalImpl,
			String inCapabilityToken, EndpointListener inListener) {
		this.context = twilioSignalImpl.getContext();
		this.listener = inListener;
		this.sigalCore = SignalCore.getInstance(this.context);
	}


	@Override
	public Endpoint initWithToken(String token, EndpointListener listener) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Endpoint initWithToken(String token, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void listen() {
		SignalCore.getInstance(this.context).register();
	}


	@Override
	public void unlisten() {
		SignalCore.getInstance(this.context).unregister(this);	
	}


	@Override
	public void leaveConversaton(Conversation conversation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setMuted(boolean muted) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setMuted(boolean muted, Conversation conversation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isMuted() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isMuted(Conversation conversation) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Map<Capability, Object> getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateCapabilityToken(String token) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setEndpointListener(EndpointListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setIncomingIntent(PendingIntent inIntent) {
		incomingIntent = inIntent;
	}

	@Override
	public Conversation createConversation(String remoteEndpoint,
			Map<String, String> options, ConversationListener linstener) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onRegistrationComplete() {
		if(this.listener != null) {
		    this.listener.onEndpointStartListeningForInvites(this);
		}
	}

	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	@Override
	public void onUnRegistrationComplete() {
		if(this.listener != null) {
		    this.listener.onEndpointStopListeningForInvites(this);
		}
	}


	@Override
	public void onIncomingCall() {
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
            TwilioSignalImpl twImpl = TwilioSignalImpl.getInstance();
            return twImpl.findDeviceByUUID(uuid);
        }

    	@Override
        public EndpointImpl[] newArray(int size)
        {
            throw new UnsupportedOperationException();
        }
    };


	@Override
	public void accept() {
		SignalCore.getInstance(context).accept(this);			
	}


	@Override
	public void onRegistrationComplete(Endpoint endpoint) {
		if(this.listener != null) {
		    this.listener.onEndpointStartListeningForInvites(this);
		}
	}
}
