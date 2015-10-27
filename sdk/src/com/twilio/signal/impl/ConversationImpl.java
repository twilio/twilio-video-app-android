package com.twilio.signal.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.webrtc.VideoCapturerAndroid;

import android.os.Handler;
import android.util.Log;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.Media;
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

	private ConversationListener conversationListener;
	private LocalMediaImpl localMediaImpl;
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
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeSessionObserver);

		@Override
		public long getNativeHandle() {
			return nativeSessionObserver;
		}

		public void dispose() {
			if (nativeSessionObserver != 0) {
				//Observer is self-destructing. Once it sends event that session has stopped it will call Release.
				//All we need to do is set nativeSessionObserver to NULL....for now...
				freeNativeObserver(nativeSessionObserver);
				nativeSessionObserver = 0;
			}
			
		}
		
	}

	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;
	
	private ConversationImpl(EndpointImpl endpoint, Set<String> participants, LocalMediaImpl localMediaImpl, ConversationListener conversationListener) {
		String[] participantAddressArray = new String[participants.size()];
		int i = 0;
		for(String participant : participants) {
			participantAddressArray[i++] = participant;
		}

		// TODO: throw an exception if the handler returns null
		handler = CallbackHandler.create();

		this.localMediaImpl = localMediaImpl;
		this.conversationListener = conversationListener;

		sessionObserverInternal = new SessionObserverInternal(this, this);

		nativeHandle = wrapOutgoingSession(endpoint.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantAddressArray);

		String deviceName = getPreferredDeviceName();
		if(deviceName != null) {
			// TODO: pass an error handler and callback on the listener to propagate camera errors
			VideoCapturerAndroid capturer = VideoCapturerAndroid.create(deviceName, null);
			long nativeVideoCapturer = getNativeVideoCapturer(capturer);
			setExternalCapturer(nativeHandle, nativeVideoCapturer);
		} else {
			// TODO: disable video or throw an exception notifying the user that there is no camera available
		}

		start();

	}

	private String getPreferredDeviceName() {
		if(VideoCapturerAndroid.getDeviceCount() == 0) {
			return null;
		}
		// Use the front-facing camera if one is available otherwise use the first available device
		String deviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
		if(deviceName == null) {
			deviceName = VideoCapturerAndroid.getDeviceName(0);
		}
		return deviceName;
	}

	private long getNativeVideoCapturer(VideoCapturerAndroid capturer) {
		// TODO: throw exceptions to callee
		// Use reflection to obtain the native video capturer handle
		long nativeVideoCapturer = 0;
		try {
			Field field = capturer.getClass().getSuperclass().getDeclaredField("nativeVideoCapturer");
			field.setAccessible(true);
			nativeVideoCapturer = field.getLong(capturer);
		} catch (Exception e) {
			logger.e(e.toString());
		}
		return nativeVideoCapturer;
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
	
	public static ConversationImpl createOutgoingConversation(EndpointImpl endpoint, Set<String> participants,
			   LocalMediaImpl localMediaImpl,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(endpoint, participants, localMediaImpl, listener);
		if (conv.getNativeHandle() == 0) {
			return null;
		}
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
		return new ConversationImpl(nativeSession, participantsAddr);
	}
	
	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getParticipants() {
		Set<String> participants =  new HashSet<String>();
		for (Participant participant : participantMap.values()) {
			participants.add(participant.getAddress());
		}
		return participants;
	}

	@Override
	public Media getLocalMedia() {
		// TODO Auto-generated method stub
		return null;
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
	public void invite(Set<String> participantAddresses) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		stop();
	}

	@Override
	public String getConversationSid() {
		// TODO Auto-generated method stub
		return null;
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
		participantMap.clear();
		dispose();
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
					final VideoTrackImpl videoTrackImpl = VideoTrackImpl.create(webRtcVideoTrack, trackInfo);
					localMediaImpl.addVideoTrack(videoTrackImpl);
					localMediaImpl.getContainerView().post(new Runnable() {
						@Override
						public void run() {
							localVideoRenderer = new VideoViewRenderer(localMediaImpl.getContainerView().getContext(), localMediaImpl.getContainerView());
							videoTrackImpl.addRenderer(localVideoRenderer);
						}
					});
				}
			}).start();
		} else {
			final ParticipantImpl participant = addParticipant(trackInfo.getParticipantAddress());
			final VideoTrackImpl videoTrackImpl = VideoTrackImpl.create(webRtcVideoTrack, trackInfo);
			participant.getMediaImpl().addVideoTrack(videoTrackImpl);
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onVideoAddedForParticipant(ConversationImpl.this, participant, videoTrackImpl);
					}
				});
			}
		}
	}

	@Override
	public void onVideoTrackRemoved(TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantAddress());
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

	/**
	 * NativeHandleInterface
	 */
	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
	
	private void dispose() {
		sessionObserverInternal.dispose();
		sessionObserverInternal = null;
		if (nativeHandle != 0) {
			//Session is self-destructing. Once Core sends event that session has stopped it will call Release.
			//All we need to do is set nativeSession to NULL....for now...
			freeNativeHandle(nativeHandle);
		}
	}
	
	public void setLocalMedia(LocalMediaImpl media) {
		localMediaImpl = media;
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
	
	@Override
	public void start() {
		start(getNativeHandle());
		
	}

	@Override
	public void stop() {
		stop(getNativeHandle());
		
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

	

}
