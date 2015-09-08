package com.twilio.signal.impl;

import java.util.Set;

import android.view.Surface;
import android.graphics.SurfaceTexture;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.Media;
import com.twilio.signal.impl.VideoSurface;
import com.twilio.signal.impl.VideoSurfaceFactory;
import com.twilio.signal.impl.logging.Logger;

public class ConversationImpl implements Conversation, NativeHandleInterface, VideoSurface.Observer {

	private Surface[] surfaces;

	static final Logger logger = Logger.getLogger(ConversationImpl.class);
	
	class SessionObserverInternal implements NativeHandleInterface {
		
		private long nativeSessionObserver;
		
		public SessionObserverInternal(ConversationListener listener, Endpoint endpoint) {
			//this.listener = listener;
			this.nativeSessionObserver = wrapNativeObserver(listener, endpoint);
		}

		private native long wrapNativeObserver(ConversationListener listener, Endpoint endpoint);
		//::TODO figure out when to call this - may be Endpoint.release() ??
		private native void freeNativeObserver(long nativeSessionObserver);

		@Override
		public long getNativeHandle() {
			return nativeSessionObserver;
		}
		
	}
	
	//private EndpointImpl endpoint;
	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;
	
	private ConversationImpl(EndpointImpl endpoint, Set<String> participants,
			   Media localMedia,
			   ConversationListener listener) {
		//this.endpoint = endpoint;
		String[] participantArray = participants.toArray(new String[participants.size()]);
		sessionObserverInternal = new SessionObserverInternal(listener, endpoint);

		/*
		 * Create a Surface from the SurfaceTexture
		 * http://stackoverflow.com/questions/24312632/how-do-you-get-anativewindow-from-a-surfacetexture-in-the-ndk
		 */
		surfaces = new Surface[localMedia.getViews().length];		
		for(int i = 0; i < surfaces.length; i++) {
			surfaces[i] = new Surface(localMedia.getViews()[i]);
		}
		nativeHandle = wrapOutgoingSession(endpoint.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantArray,
				surfaces);
	}
	
	public static Conversation create(EndpointImpl endpoint, Set<String> participants,
			   Media localMedia,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(endpoint, participants, localMedia, listener);
		if (conv.getNativeHandle() == 0) {
			//notify listener?
			return null;
		}
		VideoSurface videoSurface = VideoSurfaceFactory.createVideoSurface(conv);

		if(videoSurface == null) {
			logger.i("video surface object is null");	
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
	
	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants, Surface[] views);

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
	
	@Override
	public void onDidAddVideoTrack() { }

	@Override
	public void onDidRemoveVideoTrack() { } 

	@Override
	public void onDidReceiveVideoTrackEvent() { }

}
