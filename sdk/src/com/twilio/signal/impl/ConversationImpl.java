package com.twilio.signal.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.Media;
import com.twilio.signal.TrackOrigin;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.DisconnectReason;
import com.twilio.signal.impl.core.Event;
import com.twilio.signal.impl.core.MediaStreamInfo;
import com.twilio.signal.impl.core.SessionState;
import com.twilio.signal.impl.core.TrackInfo;
import com.twilio.signal.impl.logging.Logger;

public class ConversationImpl implements Conversation, NativeHandleInterface, SessionObserver {

	private ConversationListener conversationListener;
	private LocalMediaImpl localMediaImpl;
	private VideoViewRenderer localVideoRenderer;

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
		
	}

	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;

	private ConversationImpl(EndpointImpl endpoint, Set<String> participants, LocalMediaImpl localMediaImpl, ConversationListener conversationListener) {
		String[] participantAddressArray = new String[participants.size()];
		int i = 0;
		for(String participant : participants) {
			participantAddressArray[i++] = participant;
		}
	
		this.localMediaImpl = localMediaImpl;
		this.conversationListener = conversationListener;

		sessionObserverInternal = new SessionObserverInternal(this, this);

		nativeHandle = wrapOutgoingSession(endpoint.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantAddressArray);

		start(nativeHandle);
	}

	public static Conversation create(EndpointImpl endpoint, Set<String> participants,
			   LocalMediaImpl localMediaImpl,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(endpoint, participants, localMediaImpl, listener);
		if (conv.getNativeHandle() == 0) {
			return null;
		}
		return conv;
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getParticipants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Media getLocalMedia() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConversationListener getConversationListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConversationListener(ConversationListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invite(Set<String> participantAddresses) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		stop(nativeHandle);
	}

	@Override
	public String getConversationSid() {
		// TODO Auto-generated method stub
		return null;
	}

	private ParticipantImpl retrieveParticipant(String participantAddress) {
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
	public void onReceivedEvent(Event event) {
		logger.i("onReceivedEvent");
		
	}

	@Override
	public void onStartCompleted(CoreError error) {
		logger.i("onStartCompleted");
		
	}

	@Override
	public void onStopCompleted(CoreError error) {
		logger.i("onStartCompleted");
		if (error == null) {
			conversationListener.onConversationEnded(ConversationImpl.this);
		} else {
			final ConversationException e =
					new ConversationException(error.getDomain(), error.getCode(),
							error.getMessage());
			conversationListener.onConversationEnded(ConversationImpl.this, e);
		}
	}

	@Override
	public void onConnectParticipant(String participantAddress, CoreError error) {
		logger.i("onConnectParticipant " + participantAddress);

		ParticipantImpl participant = retrieveParticipant(participantAddress);
		if (error == null) {
			conversationListener.onConnectParticipant(this, participant);
		} else {
			ConversationException e =
					new ConversationException(error.getDomain(), error.getCode(), error.getMessage());
			conversationListener.onFailToConnectParticipant(this, participant, e);
		}
	}

	@Override
	public void onDisconnectParticipant(final String participantAddress, final DisconnectReason reason) {
		final ParticipantImpl participant = participantMap.remove(participantAddress);
		if(participant == null) {
			logger.i("participant removed but was never in list");
		} else {
			conversationListener.onDisconnectParticipant(this, participant);
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
		Conversation.Status convStatus = sessionStateToStatus(status);
		conversationListener.onLocalStatusChanged(this, convStatus);
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
			ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
			VideoTrackImpl videoTrackImpl = VideoTrackImpl.create(webRtcVideoTrack, trackInfo);
			participant.getMediaImpl().addVideoTrack(videoTrackImpl);
			conversationListener.onVideoAddedForParticipant(this, participant, videoTrackImpl);
		}
	}

	@Override
	public void onVideoTrackRemoved(TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantAddress());
		ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
		VideoTrack videoTrack = participant.getMediaImpl().removeVideoTrack(trackInfo);
		conversationListener.onVideoRemovedForParticipant(this, participant, videoTrack);
	}

	@Override
	public long getNativeHandle() {
		return nativeHandle;
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

	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);
	private native void start(long nativeSession);
	private native void stop(long nativeSession);

}
