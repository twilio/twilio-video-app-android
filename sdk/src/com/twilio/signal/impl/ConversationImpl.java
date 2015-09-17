package com.twilio.signal.impl;

import java.util.Set;
import java.util.HashSet;

import android.content.Context;

import com.twilio.signal.Conversation;
import com.twilio.signal.Participant;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.Media;
import com.twilio.signal.impl.ParticipantImpl;
import com.twilio.signal.impl.ConversationObserver;
import com.twilio.signal.impl.VideoSurface;
import com.twilio.signal.impl.VideoSurfaceFactory;
import com.twilio.signal.impl.logging.Logger;
import org.webrtc.VideoRenderer;

public class ConversationImpl implements Conversation, NativeHandleInterface, VideoSurface.Observer, ConversationObserver {

	private ConversationListener conversationListener;
	private Set<Participant> participants = new HashSet<Participant>();
	private Media localMedia;
	private VideoSurface videoSurface;


	static final Logger logger = Logger.getLogger(ConversationImpl.class);
	
	class SessionObserverInternal implements NativeHandleInterface {
		
		private long nativeSessionObserver;
		
		public SessionObserverInternal(ConversationObserver conversationObserver, Conversation conversation) {
			//this.listener = listener;
			this.nativeSessionObserver = wrapNativeObserver(conversationObserver, conversation);
		}

		private native long wrapNativeObserver(ConversationObserver conversationObserver, Conversation conversation);
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeSessionObserver);

		@Override
		public long getNativeHandle() {
			return nativeSessionObserver;
		}
		
	}

	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;

	private ConversationImpl(Context context, EndpointImpl endpoint, Set<String> participants, Media localMedia, ConversationListener conversationListener) {
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

		videoSurface = VideoSurfaceFactory.createVideoSurface(context, this);

		setVideoSurface(nativeHandle, videoSurface.getNativeHandle());

		start(nativeHandle);
	}

	public static Conversation create(Context context, EndpointImpl endpoint, Set<String> participants,
			   Media localMedia,
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

	/*
	 * ConversationListener events
	 */

	@Override
	public void onConnectParticipant(String participantAddress) {
		logger.i("onConnectParticipant " + participantAddress);
	}

	@Override
	public void onFailToConnectParticipant(String participant, int error, String errorMessage) {
		conversationListener.onFailToConnectParticipant(this, new ParticipantImpl(this, participant), error, errorMessage);
	}

	@Override
	public void onDisconnectParticipant(String participantAddress) {
		for(Participant participant : participants) {
			if(participant.getAddress().equals(participantAddress)) {
				conversationListener.onDisconnectParticipant(this, participant);
				break;
			}
		}
	}

	@Override
	public void onVideoAddedForParticipant(String participantAddress) {
		logger.i("onVideoAddedForParticipant " + participantAddress);

		Participant participant = new ParticipantImpl(this, participantAddress);
		participants.add(participant);

		conversationListener.onVideoAddedForParticipant(this, participant);
	}

	@Override
	public void onVideoRemovedForParticipant(String participantAddress) {
		logger.i("onVideoRemovedForParticipant " + participantAddress);
		for(Participant participant : participants) {
			if(participant.getAddress().equals(participantAddress)) {
				conversationListener.onVideoRemovedForParticipant(this, participant);
				break;
			}
		}
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

	/*
	 * VideoSurface.Observer Events
	 */
	@Override
	public void onDidAddVideoTrack(TrackInfo trackInfo) {

	}

	@Override
	public void onDidRemoveVideoTrack(TrackInfo trackInfo) {

	}

	@Override
	public void onDidReceiveVideoTrackEvent(VideoRenderer.I420Frame frame, TrackInfo trackInfo) {
		for(Participant participant : participants) {
			if(participant.getAddress().equals(trackInfo.getParticipantAddress())) {
				videoSurface.renderFrame(frame, participant.getMedia().getContainerView());
				return;
			}
		}
	}

	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);

	private native void setVideoSurface(long nativeSession, long nativeVideoSurface);

	private native void start(long nativeSession);

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
}
