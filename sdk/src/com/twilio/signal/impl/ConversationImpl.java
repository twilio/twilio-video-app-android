package com.twilio.signal.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.util.Log;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.Participant;
import com.twilio.signal.TrackOrigin;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.CoreSession;
import com.twilio.signal.impl.core.DisconnectReason;
import com.twilio.signal.impl.core.MediaStreamInfo;
import com.twilio.signal.impl.core.SessionState;
import com.twilio.signal.impl.core.TrackInfo;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

public class ConversationImpl implements Conversation, NativeHandleInterface, SessionObserver, CoreSession {

	private static final String DISPOSE_MESSAGE = "The conversation has been disposed. This operation is no longer valid";
	private static final String FINALIZE_MESSAGE = "Conversations must be released by calling dispose(). Failure to do so may result in leaked resources.";
	
	private ConversationListener conversationListener;
	private LocalMedia localMedia;
	private VideoViewRenderer localVideoRenderer;

	private Handler handler;

	private Map<String,ParticipantImpl> participantMap = new HashMap<String,ParticipantImpl>();

	private static String TAG = "ConversationImpl";

	static final Logger logger = Logger.getLogger(ConversationImpl.class);
	
	class SessionObserverInternal implements NativeHandleInterface {
		
		private long nativeSessionObserver;
		
		public SessionObserverInternal(SessionObserver sessionObserver, Conversation conversation) {
			//this.listener = listener;
			this.nativeSessionObserver = wrapNativeObserver(sessionObserver, conversation);
		}

		private native long wrapNativeObserver(SessionObserver sessionObserver, Conversation conversation);
		
		private native void freeNativeObserver(long nativeSessionObserver);

		@Override
		public long getNativeHandle() {
			return nativeSessionObserver;
		}

		public void dispose() {
			if (nativeSessionObserver != 0) {
				// NOTE: The core destroys the SessionObserver once it has stopped.
				// We do not need to call Release() in this case.
				freeNativeObserver(nativeSessionObserver);
				nativeSessionObserver = 0;
			}
			
		}
		
	}

	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;
	private boolean isDisposed;
	
	private ConversationImpl(ConversationsClientImpl conversationsClient, Set<String> participants, LocalMedia localMedia, ConversationListener conversationListener) {
		String[] participantAddressArray = new String[participants.size()];
		int i = 0;
		for(String participant : participants) {
			participantAddressArray[i++] = participant;
		}

		// TODO: throw an exception if the handler returns null
		handler = CallbackHandler.create();

		this.localMedia = localMedia;
		((LocalMediaImpl)localMedia).setConversation(this);
		
		this.conversationListener = conversationListener;

		sessionObserverInternal = new SessionObserverInternal(this, this);

		nativeHandle = wrapOutgoingSession(conversationsClient.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantAddressArray);

	}
	
	private ConversationImpl(long nativeSession, String[] participantsAddr) {
		nativeHandle = nativeSession;
		// TODO: throw an exception if the handler returns null
		handler = CallbackHandler.create();
		for (String participant : participantsAddr) {
			addParticipant(participant);
		}
		sessionObserverInternal = new SessionObserverInternal(this, this);
		setSessionObserver(nativeSession, sessionObserverInternal.getNativeHandle());
	}
	
	public static ConversationImpl createOutgoingConversation(ConversationsClientImpl conversationsClient, Set<String> participants,
			   LocalMedia localMedia,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(conversationsClient, participants, localMedia, listener);
		if (conv.getNativeHandle() == 0) {
			return null;
		}
		conv.start();
		return conv;
	}
	
	public static ConversationImpl createIncomingConversation(
			long nativeSession,
			String[] participantsAddr) {
		if (nativeSession == 0) {
			return null;
		}
		if (participantsAddr == null || participantsAddr.length == 0) {
			return null;
		}
		ConversationImpl conversationImpl = new ConversationImpl(nativeSession, participantsAddr);
		return conversationImpl;
	}
	
	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getParticipants() {
		checkDisposed();
		Set<String> participants =  new HashSet<String>();
		for (Participant participant : participantMap.values()) {
			participants.add(participant.getAddress());
		}
		return participants;
	}

	@Override
	public LocalMedia getLocalMedia() {
		checkDisposed();
		return localMedia;
	}

	@Override
	public ConversationListener getConversationListener() {
		return conversationListener;
	}

	@Override
	public void setConversationListener(ConversationListener listener) {
		this.conversationListener = listener;
	}

	@Override
	public void invite(Set<String> participantAddresses) throws IllegalArgumentException {
		checkDisposed();
		if ((participantAddresses == null) || (participantAddresses.size() == 0)) {
			throw new IllegalArgumentException("participantAddresses cannot be null or empty");
		}
		inviteParticipants(participantAddresses);
	}

	@Override
	public void disconnect() {
		checkDisposed();
		stop();
	}

	@Override
	public String getConversationSid() {
		// TODO Auto-generated method stub
		checkDisposed();
		return null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (isDisposed || nativeHandle == 0) {
			logger.e("YOU FORGOT TO DISPOSE NATIVE RESOURCES!");
			dispose();
		}
	}

	private ParticipantImpl addParticipant(String participantAddress) {
		ParticipantImpl participant = participantMap.get(participantAddress);
		if(participant == null) {
			participant = new ParticipantImpl(participantAddress);
			participantMap.put(participantAddress, participant);
		}
		return participant;
	}

	/*
	 * ConversationListener events
	 */


	@Override
	public void onStartCompleted(CoreError error) {
		logger.i("onStartCompleted");
		
	}

	@Override
	public void onStopCompleted(CoreError error) {
		logger.i("onStopCompleted");
		// Conversations that are rejected do not have a listener
		if(conversationListener == null) {
			return;
		}
		participantMap.clear();
		if (error == null) {
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onConversationEnded(ConversationImpl.this);
					}
				});
			}
		} else {
			final ConversationException e =
					new ConversationException(error.getDomain(), error.getCode(),
							error.getMessage());
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onConversationEnded(ConversationImpl.this, e);
					}
				});
			}
		}
	}

	@Override
	public void onConnectParticipant(String participantAddress, CoreError error) {
		logger.i("onConnectParticipant " + participantAddress);
		final ParticipantImpl participant = addParticipant(participantAddress);
		if (error == null) {
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onConnectParticipant(ConversationImpl.this, participant);
					}
				});
			}
		} else {
			final ConversationException e = new ConversationException(error.getDomain(), error.getCode(), error.getMessage());
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onFailToConnectParticipant(ConversationImpl.this, participant, e);
					}
				});
			}
		}
	}

	@Override
	public void onDisconnectParticipant(final String participantAddress, final DisconnectReason reason) {
		final ParticipantImpl participant = participantMap.remove(participantAddress);
		if(participant == null) {
			logger.i("participant removed but was never in list");
		} else {
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onDisconnectParticipant(ConversationImpl.this, participant);
					}
				});
			}
		}
	}

	@Override
	public void onMediaStreamAdded(MediaStreamInfo stream) {
		logger.i("onMediaStreamAdded");
	}

	@Override
	public void onMediaStreamRemoved(MediaStreamInfo stream) {
		logger.i("onMediaStreamRemoved");
	}

	@Override
	public void onLocalStatusChanged(final SessionState status) {
		logger.i("state changed to:"+status.name());
		final Conversation.Status convStatus = sessionStateToStatus(status);
		if(handler != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					conversationListener.onLocalStatusChanged(ConversationImpl.this, convStatus);
				}
			});
		}
	}

	@Override
	public void onVideoTrackAdded(final TrackInfo trackInfo, final org.webrtc.VideoTrack webRtcVideoTrack) {
		logger.i("onVideoTrackAdded " + trackInfo.getParticipantAddress() + " " + trackInfo.getTrackOrigin() + " " + webRtcVideoTrack.id());

		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			/*
			 * TODO: Investigate GLSurfaceView surface creation issue.
			 * GLSurfaceView onSurfaceCreated is not called in some cases.
			 * Delaying the creation of the local video renderer alleviates the problem.
			 */
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch(Exception e) {

					}
					Log.i(TAG, "Local Adding Renderer");
					List<LocalVideoTrack> tracksList = localMedia.getLocalVideoTracks();
					if (tracksList.size() == 0) {
						// TODO: throw error to the user
					}
					final LocalVideoTrackImpl videoTrackImpl = (LocalVideoTrackImpl)tracksList.get(0);
					videoTrackImpl.setWebrtcVideoTrack(webRtcVideoTrack);
					videoTrackImpl.setTrackInfo(trackInfo);
					localMedia.getContainerView().post(new Runnable() {
						@Override
						public void run() {
							localVideoRenderer = new VideoViewRenderer(localMedia.getContainerView().getContext(), localMedia.getContainerView());
							videoTrackImpl.addRenderer(localVideoRenderer);
						}
					});
					if (handler != null) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								if (conversationListener != null) {
									conversationListener.onLocalVideoAdded(
											ConversationImpl.this, videoTrackImpl);
								}
							}
						});
					}
				}
			}).start();
		} else {
			final ParticipantImpl participant = addParticipant(trackInfo.getParticipantAddress());
			final VideoTrackImpl videoTrackImpl = new VideoTrackImpl(webRtcVideoTrack, trackInfo);
			participant.getMediaImpl().addVideoTrack(videoTrackImpl);
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (conversationListener != null) {
							conversationListener.onVideoAddedForParticipant(
									ConversationImpl.this, participant, videoTrackImpl);
						}
					}
				});
			}
		}
	}

	@Override
	public void onVideoTrackRemoved(TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantAddress());
		final TrackInfo info = trackInfo;
		if (info.getTrackOrigin() == TrackOrigin.LOCAL) {
			LocalMediaImpl localMediaImpl = (LocalMediaImpl)localMedia;
			final LocalVideoTrackImpl videoTrack =
					localMediaImpl.removeLocalVideoTrack(trackInfo);
			videoTrack.removeCameraCapturer();
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onLocalVideoRemoved(ConversationImpl.this, videoTrack);
					}
				});
			}
			
		} else {
			final ParticipantImpl participant = addParticipant(trackInfo.getParticipantAddress());
			final VideoTrack videoTrack = participant.getMediaImpl().removeVideoTrack(trackInfo);
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onVideoRemovedForParticipant(ConversationImpl.this, participant, videoTrack);
					}
				});
			}
		}
		
		
	}

	/**
	 * NativeHandleInterface
	 */
	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
	
	@Override
	public synchronized void dispose() {
		if (sessionObserverInternal != null) {
			sessionObserverInternal.dispose();
			sessionObserverInternal = null;
		}
		if (nativeHandle != 0) {
			// NOTE: The core destroys the Session once it has stopped.
			// We do not need to call Release() in this case.
			freeNativeHandle(nativeHandle);
			nativeHandle = 0;
		}
	}
	
	public void setLocalMedia(LocalMedia media) {
		checkDisposed();
		localMedia = media;
		((LocalMediaImpl)localMedia).setConversation(this);
	}
	
	private Conversation.Status sessionStateToStatus(SessionState state) {
		switch(state) {
			case INITIALIZED:
			case STARTING:
				return Status.CONNECTING;
			case IN_PROGRESS:
			case STOPPING:
			case STOP_FAILED:
				return Status.CONNECTED;
			case STOPPED:
				return Status.DISCONNECTED;
			case START_FAILED:
				return Status.FAILED;
			default:
				return Status.UNKNOWN;
		}
	}
	
	void setupExternalCapturer()  {
		try {
			LocalVideoTrack localVideoTrack = localMedia.getLocalVideoTracks().get(0);
			CameraCapturerImpl cameraCapturer = (CameraCapturerImpl)localVideoTrack.getCameraCapturer();
			if (cameraCapturer != null) {
				cameraCapturer.startConversationCapturer();
				setExternalCapturer(nativeHandle, cameraCapturer.getNativeVideoCapturer());
			} else {
				//TODO : we should throw exception only in case when local video is selected and
				// camera is not present GSDK-272
			}
			
		} catch (NullPointerException e) {
			logger.e("Failed to obtain local video track");
			// TODO : throw custom exception
		} catch (IndexOutOfBoundsException e) {
			logger.e("Failed to obtain local video track");
			// TODO : throw custom exception
		}
	}
	
	boolean mute(boolean on) {
		return mute(getNativeHandle(), on);
	}
	
	boolean isMuted() {
		return isMuted(getNativeHandle());
	}
	
	/**
	 * CoreSession
	 */
	@Override
	public void start() {
		logger.d("starting call");
	
		// TODO: Call only when video is enabled
		setupExternalCapturer();


		start(getNativeHandle());
		
	}

	@Override
	public void stop() {
		stop(getNativeHandle());
	}
	
	@Override
	public boolean enableVideo(boolean enabled, boolean paused) {
		return enableVideo(getNativeHandle(), enabled, paused);
	}
	

	@Override
	public void inviteParticipants(Set<String> participants) {
		String[] participantAddressArray =
				participants.toArray(new String[participants.size()]);
		inviteParticipants(getNativeHandle(), participantAddressArray);
	}

	
	private synchronized void checkDisposed() {
		if (isDisposed || nativeHandle == 0) {
			throw new IllegalStateException(DISPOSE_MESSAGE);
		}
	}
	
	/**
	 * Native methods
	 */
	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);
	private native void start(long nativeSession);
	private native void setExternalCapturer(long nativeSession, long nativeCapturer);
	private native void stop(long nativeSession);
	private native void setSessionObserver(long nativeSession, long nativeSessionObserver);
	private native void freeNativeHandle(long nativeHandle);
	private native boolean enableVideo(long nativeHandle, boolean enabled, boolean paused);
	private native boolean mute(long nativeSession, boolean on);
	private native boolean isMuted(long nativeSession);
	private native void inviteParticipants(long nativeHandle, String[] participants);

	

}
