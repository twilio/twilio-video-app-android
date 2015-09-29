package com.twilio.signal.impl;

import android.app.Activity;
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
import com.twilio.signal.LocalMediaImpl;
import com.twilio.signal.TrackOrigin;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.ParticipantImpl;
import com.twilio.signal.impl.SessionObserver;
import com.twilio.signal.impl.TrackInfo;
import com.twilio.signal.impl.logging.Logger;

public class ConversationImpl implements Conversation, NativeHandleInterface, SessionObserver {

	private ConversationListener conversationListener;
	private LocalMediaImpl localMediaImpl;
	private VideoViewRenderer localVideoRenderer;
	private Activity activity;

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

	private ConversationImpl(Activity activity, EndpointImpl endpoint, Set<String> participants, LocalMediaImpl localMediaImpl, ConversationListener conversationListener) {
		this.activity = activity;
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

	public static Conversation create(Activity activity, EndpointImpl endpoint, Set<String> participants,
			   LocalMediaImpl localMediaImpl,
			   ConversationListener listener) {
		ConversationImpl conv = new ConversationImpl(activity, endpoint, participants, localMediaImpl, listener);
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
	public void onFailToConnectParticipant(final String participant, final int error, final String errorMessage) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				conversationListener.onFailToConnectParticipant(ConversationImpl.this, new ParticipantImpl(participant), error, errorMessage);
			}
		});
	}

	@Override
	public void onDisconnectParticipant(final String participantAddress) {
		final ParticipantImpl participant = participantMap.remove(participantAddress);
		if(participant == null) {
			logger.i("participant removed but was never in list");
		} else {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					conversationListener.onDisconnectParticipant(ConversationImpl.this, participant);
				}
			});
		}
	}

	@Override
	public void onVideoAddedForParticipant(String participantAddress) {

	}

	@Override
	public void onVideoRemovedForParticipant(String participantAddress) {

	}

	@Override
	public void onLocalStatusChanged(final Status status) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				conversationListener.onLocalStatusChanged(ConversationImpl.this, status);
			}
		});
	}
	
	@Override
	public void onConversationEnded() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				conversationListener.onConversationEnded(ConversationImpl.this);
			}
		});
	}

	@Override
	public void onConversationEnded(final int error, final String errorMessage) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				conversationListener.onConversationEnded(ConversationImpl.this, error, errorMessage);
			}
		});

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

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							localVideoRenderer = new VideoViewRenderer(activity, localMediaImpl.getContainerView());
							videoTrackImpl.addRenderer(localVideoRenderer);
						}
					});
				}
			}).start();
		} else {
			final ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
			final VideoTrackImpl videoTrackImpl = VideoTrackImpl.create(webRtcVideoTrack, trackInfo);
			participant.getMediaImpl().addVideoTrack(videoTrackImpl);

			activity.runOnUiThread(new Runnable() {
        			@Override
        			public void run() {
					conversationListener.onVideoAddedForParticipant(ConversationImpl.this, participant, videoTrackImpl); 
        			}
			});
		}
	}

	@Override
	public void onVideoTrackRemoved(TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantAddress());
		final ParticipantImpl participant = retrieveParticipant(trackInfo.getParticipantAddress());
		final VideoTrack videoTrack = participant.getMediaImpl().removeVideoTrack(trackInfo);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				conversationListener.onVideoRemovedForParticipant(ConversationImpl.this, participant, videoTrack);
			}
		});
	}

	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);

	private native void start(long nativeSession);

	@Override
	public long getNativeHandle() {
		return nativeHandle;
	}
}
