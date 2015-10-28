package com.twilio.signal.impl;

import java.util.Set;
import java.util.UUID;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.impl.core.CoreEndpoint;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.EndpointObserver;
import com.twilio.signal.impl.core.EndpointState;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

public class EndpointImpl implements
						Endpoint,
						NativeHandleInterface,
						Parcelable,
						EndpointObserver,
						CoreEndpoint{

	static final Logger logger = Logger.getLogger(EndpointImpl.class);
	
	class EndpointObserverInternal implements NativeHandleInterface {

		private long nativeEndpointObserver;
		
		public EndpointObserverInternal(EndpointObserver observer) {
			//this.listener = listener;
			this.nativeEndpointObserver = wrapNativeObserver(observer, EndpointImpl.this);
		}

		private native long wrapNativeObserver(EndpointObserver observer, Endpoint endpoint);
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeEndpointObserver);

		@Override
		public long getNativeHandle() {
			return nativeEndpointObserver;
		}



		public void dispose() {
			if (nativeEndpointObserver != 0) {
				freeNativeObserver(nativeEndpointObserver);
				nativeEndpointObserver = 0;
			}
		}
		
	}

	private final UUID uuid = UUID.randomUUID();
	private Context context;
	private EndpointListener listener;
	private String userName;
	private PendingIntent incomingIntent = null;
	private EndpointObserverInternal endpointObserver;
	private long nativeEndpointHandle;
	private boolean isDisposed;

	private Handler handler;
	private EndpointState coreState;
	
	public UUID getUuid() {
		return uuid;
	}


	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (isDisposed || nativeEndpointHandle == 0) {
			logger.e("YOU FORGOT TO DISPOSE NATIVE RESOURCES!");
			dispose();
		}
	}


	EndpointImpl(Context context,
			EndpointListener inListener) {
		this.context = context;
		this.listener = inListener;

		this.endpointObserver = new EndpointObserverInternal(this);
		// TODO: throw an exception if the handler returns null
		handler = CallbackHandler.create();
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
		checkDisposed("Can't perform listen if native object is disposed.");
		listen(nativeEndpointHandle);
	}


	@Override
	public void unlisten() {
		//SignalCore.getInstance(this.context).unregister(this);
		checkDisposed("Can't perform unlisten if native object is disposed.");
		unlisten(nativeEndpointHandle);
	}




	@Override
	public void setEndpointListener(EndpointListener listener) {
		this.listener = listener;

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
			LocalMediaImpl localMediaImpl, ConversationListener listener) {
		checkDisposed("Can't create conversation if native object is disposed.");
		Conversation conv = ConversationImpl.createOutgoingConversation(
				this, participants, localMediaImpl, listener);
		return conv;
	}
	
	@Override
	public synchronized void dispose() {
		if (!isDisposed && nativeEndpointHandle != 0) {
			endpointObserver.dispose();
			endpointObserver = null;
			freeNativeHandle(nativeEndpointHandle);
			nativeEndpointHandle = 0;
			isDisposed = true;
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

	/**
	 * NativeHandleInterface
	 */
	@Override
	public long getNativeHandle() {
		return nativeEndpointHandle;
	}
	
	/**
	 * EndpointObserver methods
	 */
	@Override
	public void onRegistrationDidComplete(CoreError error) {
		logger.d("onRegistrationDidComplete");
		if (error != null) {
			final ConversationException e =
					new ConversationException(error.getDomain(),
							error.getCode(), error.getMessage());
			if (handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						listener.onFailedToStartListening(EndpointImpl.this, e);
					}
				});
			}
		} else {
			if (handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						listener.onStartListeningForInvites(EndpointImpl.this);
					}
				});
			}
		}
	}


	@Override
	public void onUnregistrationDidComplete(CoreError error) {
		logger.d("onUnregistrationDidComplete");
		if (handler != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onStopListeningForInvites(EndpointImpl.this);
				}
			});
		}
		
	}


	@Override
	public void onStateDidChange(EndpointState state) {
		logger.d("onStateDidChange");
		coreState = state;
	}


	@Override
	public void onIncomingCallDidReceive(long nativeSession,
			String[] participants) {
		logger.d("onIncomingCallDidReceive");
		
		ConversationImpl conv =
				ConversationImpl.createIncomingConversation(nativeSession, participants);
		if (conv == null) {
			logger.e("Failed to create conversation");
		}
		
		final Invite invite = InviteImpl.create(conv,this, participants);
		if (invite == null) {
			logger.e("Failed to create Conversation Invite");
		}
		if (handler != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onReceiveConversationInvite(EndpointImpl.this, invite);
				}
			});
		}
	}


	/*
	 * CoreEndpoint methods
	 */
	@Override
	public void accept(ConversationImpl conv) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reject(ConversationImpl conv) {
		reject(getNativeHandle(), conv.getNativeHandle());
	}


	@Override
	public void ignore(ConversationImpl conv) {
		// TODO Auto-generated method stub
	}
	
	private synchronized void checkDisposed(String errorMessage) {
		if (isDisposed || nativeEndpointHandle == 0) {
			throw new IllegalStateException(errorMessage);
		}
	}

	
	
	//Native implementation
	private native void listen(long nativeEndpoint);
	private native void unlisten(long nativeEndpoint);
	private native void reject(long nativeEndpoint, long nativeSession);
	private native void freeNativeHandle(long nativeEndpoint);


}
