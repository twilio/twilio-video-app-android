package com.twilio.signal.impl;

import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.Media;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.TrackOrigin;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.ParticipantImpl;
import com.twilio.signal.impl.SessionObserver;
import com.twilio.signal.impl.logging.Logger;

public class ConversationImpl implements Conversation, NativeHandleInterface, SessionObserver {

	private ConversationListener conversationListener;
	private LocalMedia localMedia;
	private VideoViewRenderer localVideoRenderer;
	private Context context;

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

	private ConversationImpl(Context context, EndpointImpl endpoint, Set<String> participants, LocalMedia localMedia, ConversationListener conversationListener) {
		this.context = context;
		String[] participantAddressArray = new String[participants.size()];
		int i = 0;
		for(String participant : participants) {
			participantAddressArray[i++] = participant;
		}
	
		this.localMedia = localMedia;
		this.conversationListener = conversationListener;

		sessionObserverInternal = new SessionObserverInternal(this, this);

		nativeHandle = wrapOutgoingSession(endpoint.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantAddressArray);

		start(nativeHandle);
	}

	public static Conversation create(Context context, EndpointImpl endpoint, Set<String> participants,
			   LocalMedia localMedia,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(context, endpoint, participants, localMedia, listener);
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
	public void onConnectParticipant(String participantAddress) {
		logger.i("onConnectParticipant " + participantAddress);
		retrieveParticipant(participantAddress);
	}

	@Override
	public void onFailToConnectParticipant(String participant, int error, String errorMessage) {
		conversationListener.onFailToConnectParticipant(this, new ParticipantImpl(participant), error, errorMessage);
	}

	@Override
	public void onDisconnectParticipant(String participantAddress) {
		ParticipantImpl participant = participantMap.remove(participantAddress);
		if(participant == null) {
			logger.i("participant removed but was never in list");
		} else {
			conversationListener.onDisconnectParticipant(this, participant);
		}
	}

	@Override
	public void onVideoAddedForParticipant(String participantAddress) {
	}

	@Override
	public void onVideoRemovedForParticipant(String participantAddress) {
	}

	@Override
	public void onLocalStatusChanged(Status status) {
		conversationListener.onLocalStatusChanged(this, status);
	}
	
	@Override
	public void onConversationEnded() {
		conversationListener.onConversationEnded(this);
	}

	@Override
	public void onConversationEnded(int error, String errorMessage) {
		conversationListener.onConversationEnded(this, error, errorMessage);
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
					localVideoRenderer = new VideoViewRenderer(context, localMedia.getContainerView());
					VideoTrackImpl.create(webRtcVideoTrack).addRenderer(localVideoRenderer);
				}
			}).start();
		} else {
			ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
			VideoTrack videoTrack = VideoTrackImpl.create(webRtcVideoTrack);
			participant.getMediaImpl().addVideoTrack(videoTrack);
			conversationListener.onVideoAddedForParticipant(this, participant, videoTrack); 
		}
	}

	@Override
	public void onVideoTrackRemoved(TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantAddress());
		ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
		// TODO: remove track from participant media
		conversationListener.onVideoRemovedForParticipant(this, participant);
	}

	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);

	private native void start(long nativeSession);

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
}
