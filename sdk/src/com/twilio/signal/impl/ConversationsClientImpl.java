package com.twilio.signal.impl;

import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.twilio.common.TwilioAccessManager;
import com.twilio.signal.AudioOutput;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationCallback;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.ConversationsClient;
import com.twilio.signal.ConversationsClientListener;
import com.twilio.signal.Invite;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.OutgoingInvite;
import com.twilio.signal.Participant;
import com.twilio.signal.impl.core.CoreEndpoint;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.EndpointObserver;
import com.twilio.signal.impl.core.EndpointState;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

public class ConversationsClientImpl implements
		ConversationsClient,
		NativeHandleInterface,
		Parcelable,
		EndpointObserver,
		CoreEndpoint, ConversationListener {

	static final Logger logger = Logger.getLogger(ConversationsClientImpl.class);
	
	private static final String DISPOSE_MESSAGE = "The ConversationsClient has been disposed. This operation is no longer valid";
	private static final String FINALIZE_MESSAGE = "The ConversationsClient must be released by calling dispose(). Failure to do so may result in leaked resources.";

	class EndpointObserverInternal implements NativeHandleInterface {

		private long nativeEndpointObserver;
		
		public EndpointObserverInternal(EndpointObserver observer) {
			this.nativeEndpointObserver = wrapNativeObserver(observer, ConversationsClientImpl.this);
		}

		private native long wrapNativeObserver(EndpointObserver observer, ConversationsClient conversationsClient);
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
	private ConversationsClientListener conversationsClientListener;
	private EndpointObserverInternal endpointObserver;
	private long nativeEndpointHandle;
	private boolean isDisposed;
	private boolean listening = false;
	private TwilioAccessManager accessManager;
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
			logger.e(FINALIZE_MESSAGE);
			dispose();
		}
	}


	ConversationsClientImpl(Context context,
							TwilioAccessManager accessManager,
							ConversationsClientListener conversationsClientListener) {
		this.context = context;
		this.conversationsClientListener = conversationsClientListener;
		this.accessManager = accessManager;

		this.endpointObserver = new EndpointObserverInternal(this);

		handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}
	}

	void setNativeEndpointHandle(long nativeEndpointHandle) {
		this.nativeEndpointHandle = nativeEndpointHandle;
	}
	
	long getEndpointObserverHandle() {
		return this.endpointObserver.getNativeHandle();
	}

	@Override
	public void listen() {
		checkDisposed();
		listen(nativeEndpointHandle);
	}

	@Override
	public void unlisten() {
		checkDisposed();
		unlisten(nativeEndpointHandle);
	}

	@Override
	public void setConversationsClientListener(ConversationsClientListener listener) {
		this.conversationsClientListener = listener;
	}

	@Override
	public String getIdentity() {
		return new String(accessManager.getIdentity());
	}

	@Override
	public boolean isListening() {
		return listening;
	}

	@Override
	public OutgoingInvite sendConversationInvite(Set<String> participants, LocalMedia localMedia, ConversationCallback conversationCallback) {
		checkDisposed();
		ConversationImpl outgoingConversationImpl = ConversationImpl.createOutgoingConversation(
				this, participants, localMedia, this);
		return OutgoingInviteImpl.create(this, outgoingConversationImpl, conversationCallback);
	}

	@Override
	public void onParticipantConnected(Conversation conversation, Participant participant) {

	}

	@Override
	public void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e) {

	}

	@Override
	public void onParticipantDisconnected(Conversation conversation, Participant participant) {

	}

	@Override
	public void onConversationEnded(Conversation conversation, ConversationException e) {

	}

	@Override
	public synchronized void dispose() {
		if (endpointObserver != null) {
			endpointObserver.dispose();
			endpointObserver = null;
		}
		if (!isDisposed && nativeEndpointHandle != 0) {
			freeNativeHandle(nativeEndpointHandle);
			nativeEndpointHandle = 0;
			isDisposed = true;
		}
	}

	@Override /* Parcelable */
	public int describeContents() {
        return 0;
    }

	@Override /* Parcelable */
    public void writeToParcel(Parcel out, int flags) {
        out.writeSerializable(uuid);
    }

	/* Parcelable */
    public static final Parcelable.Creator<ConversationsClientImpl> CREATOR = new Parcelable.Creator<ConversationsClientImpl>() {
    	@Override
        public ConversationsClientImpl createFromParcel(Parcel in) {
            UUID uuid = (UUID)in.readSerializable();
            TwilioConversationsImpl twilioConversations = TwilioConversationsImpl.getInstance();
            return twilioConversations.findDeviceByUUID(uuid);
        }

    	@Override
        public ConversationsClientImpl[] newArray(int size)
        {
            throw new UnsupportedOperationException();
        }
    };

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
			listening = false;
			final ConversationException e =
					new ConversationException(error.getDomain(),
							error.getCode(), error.getMessage());
			if (handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationsClientListener.onFailedToStartListening(ConversationsClientImpl.this, e);
					}
				});
			}
		} else {
			listening = true;
			if (handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationsClientListener.onStartListeningForInvites(ConversationsClientImpl.this);
					}
				});
			}
		}
	}

	@Override
	public void onUnregistrationDidComplete(CoreError error) {
		logger.d("onUnregistrationDidComplete");
		listening = false;
		if (handler != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					conversationsClientListener.onStopListeningForInvites(ConversationsClientImpl.this);
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

		ConversationImpl incomingConversation = ConversationImpl.createIncomingConversation(nativeSession, participants);
		if (incomingConversation == null) {
			logger.e("Failed to create conversation");
		}

		final Invite invite = InviteImpl.create(incomingConversation,this, participants);
		if (invite == null) {
			logger.e("Failed to create Conversation Invite");
		}
		if (handler != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					conversationsClientListener.onReceiveConversationInvite(ConversationsClientImpl.this, invite);
				}
			});
		}
	}

	/*
	 * CoreEndpoint methods
	 */
	@Override
	public void accept(ConversationImpl conv) {
		// Do nothing. This is handled in InviteImpl.
	}

	@Override
	public void reject(ConversationImpl conv) {
		reject(getNativeHandle(), conv.getNativeHandle());
	}

	@Override
	public void ignore(ConversationImpl conv) {
		// Do nothing.
	}
	
	@Override
	public void setAudioOutput(AudioOutput audioOutput) {
		logger.d("setAudioOutput");
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (audioOutput == AudioOutput.SPEAKERPHONE) {
			audioManager.setSpeakerphoneOn(true);
		} else {
			audioManager.setSpeakerphoneOn(false);
		}
		
	}
	
	@Override
	public AudioOutput getAudioOutput() {
		logger.d("getAudioOutput");
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		return audioManager.isSpeakerphoneOn() ? AudioOutput.SPEAKERPHONE : AudioOutput.HEADSET;
	}

	
	private synchronized void checkDisposed() {
		if (isDisposed || nativeEndpointHandle == 0) {
			throw new IllegalStateException(DISPOSE_MESSAGE);
		}
	}

	private native void listen(long nativeEndpoint);
	private native void unlisten(long nativeEndpoint);
	private native void reject(long nativeEndpoint, long nativeSession);
	private native void freeNativeHandle(long nativeEndpoint);

	
	


}
