package com.twilio.signal.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.os.Handler;

import com.twilio.signal.AudioTrack;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.Media;
import com.twilio.signal.Participant;
import com.twilio.signal.TrackOrigin;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.CoreSession;
import com.twilio.signal.impl.core.CoreSessionMediaConstraints;
import com.twilio.signal.impl.core.DisconnectReason;
import com.twilio.signal.impl.core.MediaStreamInfo;
import com.twilio.signal.impl.core.SessionObserver;
import com.twilio.signal.impl.core.SessionState;
import com.twilio.signal.impl.core.TrackInfo;
import com.twilio.signal.impl.logging.Logger;
import com.twilio.signal.impl.util.CallbackHandler;

public class ConversationImpl implements Conversation, NativeHandleInterface, SessionObserver, CoreSession {

	private static final String DISPOSE_MESSAGE = "The conversation has been disposed. This operation is no longer valid";
	private static final String FINALIZE_MESSAGE = "Conversations must be released by calling dispose(). Failure to do so may result in leaked resources.";
	
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
				freeNativeObserver(nativeSessionObserver);
				nativeSessionObserver = 0;
			}
			
		}
		
	}

	private SessionObserverInternal sessionObserverInternal;
	private long nativeHandle;
	private boolean isDisposed;
	
	private ConversationImpl(ConversationsClientImpl conversationsClient, Set<String> participants, LocalMedia localMedia, ConversationListener conversationListener) {
		String[] participantIdentityArray = new String[participants.size()];
		int i = 0;
		for(String participant : participants) {
			participantIdentityArray[i++] = participant;
		}

		handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}

		localMediaImpl = (LocalMediaImpl)localMedia;
		localMediaImpl.setConversation(this);
		
		this.conversationListener = conversationListener;

		sessionObserverInternal = new SessionObserverInternal(this, this);

		nativeHandle = wrapOutgoingSession(conversationsClient.getNativeHandle(),
				sessionObserverInternal.getNativeHandle(),
				participantIdentityArray);

	}
	
	private ConversationImpl(long nativeSession, String[] participantsIdentities) {
		nativeHandle = nativeSession;

		handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}

		for (String participantIdentity : participantsIdentities) {
			findOrCreateParticipant(participantIdentity, null);
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
		boolean enableVideo = !localMedia.getLocalVideoTracks().isEmpty();
		boolean pauseVideo = false;
		if (enableVideo) {
			pauseVideo = !localMedia.getLocalVideoTracks().get(0).isCameraEnabled();
		}
		CoreSessionMediaConstraints mediaContext =
				new CoreSessionMediaConstraints(localMedia.isMicrophoneAdded(),
							localMedia.isMuted(), enableVideo, pauseVideo);
		conv.start(mediaContext);
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
	public Set<String> getParticipants() {
		checkDisposed();
		Set<String> participants =  new HashSet<String>();
		for (Participant participant : participantMap.values()) {
			participants.add(participant.getIdentity());
		}
		return participants;
	}

	@Override
	public LocalMedia getLocalMedia() {
		checkDisposed();
		return localMediaImpl;
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
	public void invite(Set<String> participantIdentityes) throws IllegalArgumentException {
		checkDisposed();
		if ((participantIdentityes == null) || (participantIdentityes.size() == 0)) {
			throw new IllegalArgumentException("participantIdentityes cannot be null or empty");
		}
		inviteParticipants(participantIdentityes);
	}

	@Override
	public void disconnect() {
		checkDisposed();
		stop();
	}

	@Override
	public String getConversationSid() {
		checkDisposed();
		String conversationSid = getConversationSid(nativeHandle);
		if(conversationSid == null || conversationSid.length() == 0) {
			return null;
		} else {
			return conversationSid;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (isDisposed || nativeHandle == 0) {
			logger.e(FINALIZE_MESSAGE);
			dispose();
		}
	}

	private ParticipantImpl findOrCreateParticipant(String participantIdentity, String participantSid) {
		ParticipantImpl participant = participantMap.get(participantIdentity);
		if(participant == null) {
			logger.d("Creating new participant" + participantIdentity);
			participant = new ParticipantImpl(participantIdentity, participantSid);
			participantMap.put(participantIdentity, participant);
		}
		return participant;
	}

	/*
	 * SessionObserver events
	 */
	@Override
	public void onSessionStateChanged(SessionState status) {
		logger.i("state changed to: " + status.name());
	}

	@Override
	public void onStartCompleted(CoreError error) {
		logger.i("onStartCompleted");
	}

	@Override
	public void onStopCompleted(CoreError error) {
		// Block this thread until the handler has completed its work.
		final CountDownLatch waitLatch = new CountDownLatch(1);
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
						conversationListener.onConversationEnded(ConversationImpl.this, null);
						waitLatch.countDown();
					}
				});
			} else {
				waitLatch.countDown();
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
						waitLatch.countDown();
					}
				});
			} else {
				waitLatch.countDown();
			}
		}
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParticipantConnected(String participantIdentity, String participantSid, CoreError error) {
		// Block this thread until the handler has completed its work.
		final CountDownLatch waitLatch = new CountDownLatch(1);
		logger.i("onParticipantConnected " + participantIdentity);
		final ParticipantImpl participantImpl = findOrCreateParticipant(participantIdentity, participantSid);
		if (error == null) {
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onParticipantConnected(ConversationImpl.this, participantImpl);
						waitLatch.countDown();
						/*
						 * Workaround for CSDK-225. MediaTracks are added before onParticipantConnected is called.
						 */
						Media media = participantImpl.getMediaImpl();
						for(final VideoTrack videoTrack : media.getVideoTracks()) {
							final Handler participantHandler = participantImpl.getHandler();
							if(participantHandler != null) {
								participantHandler.post(new Runnable() {
									@Override
									public void run() {
										if (participantImpl.getParticipantListener() != null) {
											participantImpl.getParticipantListener().onVideoTrackAdded(
													ConversationImpl.this, participantImpl, videoTrack);
										}
									}
								});
							}
						}
					}
				});
			} else {
				waitLatch.countDown();
			}
		} else {
			final ConversationException e = new ConversationException(error.getDomain(), error.getCode(), error.getMessage());
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onFailedToConnectParticipant(ConversationImpl.this, participantImpl, e);
						waitLatch.countDown();
					}
				});
			} else {
				waitLatch.countDown();
			}
		}
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParticipantDisconnected(final String participantIdentity, String participantSid, final DisconnectReason reason) {
		// Block this thread until the handler has completed its work.
		final CountDownLatch waitLatch = new CountDownLatch(1);
		logger.i("onParticipantDisconnected " + participantIdentity);
		final ParticipantImpl participant = participantMap.remove(participantIdentity);
		if(participant == null) {
			logger.i("participant removed but was never in list");
			waitLatch.countDown();
		} else {
			if(handler != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						conversationListener.onParticipantDisconnected(ConversationImpl.this, participant);
						waitLatch.countDown();
					}
				});
			} else {
				waitLatch.countDown();
			}
		}
		try {
			waitLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	public void onVideoTrackAdded(final TrackInfo trackInfo, final org.webrtc.VideoTrack webRtcVideoTrack) {
		logger.i("onVideoTrackAdded " + trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackOrigin() + " " + webRtcVideoTrack.id());

		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			List<LocalVideoTrack> tracksList = localMediaImpl.getLocalVideoTracks();
			final LocalVideoTrackImpl videoTrackImpl = (LocalVideoTrackImpl)tracksList.get(0);
			videoTrackImpl.setWebrtcVideoTrack(webRtcVideoTrack);
			videoTrackImpl.setTrackInfo(trackInfo);
			if (localMediaImpl.getHandler() != null) {
				localMediaImpl.getHandler().post(new Runnable() {
					@Override
					public void run() {
						if (localMediaImpl.getLocalMediaListener() != null) {
							localMediaImpl.getLocalMediaListener().onLocalVideoTrackAdded(
									ConversationImpl.this, videoTrackImpl);
						}
					}
				});
			}
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			final VideoTrackImpl videoTrackImpl = new VideoTrackImpl(webRtcVideoTrack, trackInfo);
			participantImpl.getMediaImpl().addVideoTrack(videoTrackImpl);
			final Handler participantHandler = participantImpl.getHandler();
			if(participantHandler != null) {
				participantHandler.post(new Runnable() {
					@Override
					public void run() {
						if (participantImpl.getParticipantListener() != null) {
							participantImpl.getParticipantListener().onVideoTrackAdded(
									ConversationImpl.this, participantImpl, videoTrackImpl);
						}
					}
				});
			}
		}
	}

	@Override
	public void onVideoTrackRemoved(final TrackInfo trackInfo) {
		logger.i("onVideoTrackRemoved " + trackInfo.getParticipantIdentity());
		if (trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			final LocalVideoTrackImpl videoTrack =
					localMediaImpl.removeLocalVideoTrack(trackInfo);
			videoTrack.removeCameraCapturer();
			if(localMediaImpl.getHandler() != null) {
				localMediaImpl.getHandler().post(new Runnable() {
					@Override
					public void run() {
						if (localMediaImpl.getLocalMediaListener() != null) {
							localMediaImpl.getLocalMediaListener().onLocalVideoTrackRemoved(ConversationImpl.this, videoTrack);
						}
					}
				});
			}
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			final VideoTrack videoTrack = participantImpl.getMediaImpl().removeVideoTrack(trackInfo);
			final Handler participantHandler = participantImpl.getHandler();
			if(participantHandler != null) {
				participantHandler.post(new Runnable() {
					@Override
					public void run() {
						if (participantImpl.getParticipantListener() != null) {
							participantImpl.getParticipantListener().onVideoTrackRemoved(ConversationImpl.this, participantImpl, videoTrack);
						}
					}
				});
			}
		}
	}

	@Override
	public void onVideoTrackStateChanged(final TrackInfo trackInfo) {
		logger.i("onVideoTrackStateChanged " + trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId());
		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			return;
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			List<VideoTrack> videoTracks = participantImpl.getMediaImpl().getVideoTracks();
			for(final VideoTrack videoTrack: videoTracks) {
				if(trackInfo.getTrackId().equals(videoTrack.getTrackId())) {
					((VideoTrackImpl)videoTrack).updateTrackInfo(trackInfo);
					final Handler participantHandler = participantImpl.getHandler();
					if(participantHandler != null) {
						participantHandler.post(new Runnable() {
							@Override
							public void run() {
								if (participantImpl.getParticipantListener() != null) {
									if(trackInfo.isEnabled()) {
										participantImpl.getParticipantListener().onTrackEnabled(ConversationImpl.this, participantImpl, videoTrack);
									} else {
										participantImpl.getParticipantListener().onTrackDisabled(ConversationImpl.this, participantImpl, videoTrack);
									}
								}
							}
						});
					}
					break;
				}
			}
		}
	}

	@Override
	public void onAudioTrackAdded(TrackInfo trackInfo, final org.webrtc.AudioTrack webRtcAudioTrack) {
		logger.i("onAudioTrackAdded " + trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackOrigin() + " " + webRtcAudioTrack.id());

		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			// TODO: expose audio tracks in local media
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			final AudioTrackImpl audioTrackImpl = new AudioTrackImpl(webRtcAudioTrack, trackInfo);
			participantImpl.getMediaImpl().addAudioTrack(audioTrackImpl);
			final Handler participantHandler = participantImpl.getHandler();
			if(participantHandler != null) {
				participantHandler.post(new Runnable() {
					@Override
					public void run() {
						if (participantImpl.getParticipantListener() != null) {
							participantImpl.getParticipantListener().onAudioTrackAdded(
									ConversationImpl.this, participantImpl, audioTrackImpl);
						}
					}
				});
			}
		}
	}

	@Override
	public void onAudioTrackRemoved(TrackInfo trackInfo) {
		logger.i("onAudioTrackRemoved " + trackInfo.getParticipantIdentity());

		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			// TODO: remove audio track from local media once audio tracks are exposed
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			final AudioTrack audioTrack = participantImpl.getMediaImpl().removeAudioTrack(trackInfo);
			final Handler participantHandler = participantImpl.getHandler();
			if(participantHandler != null) {
				participantHandler.post(new Runnable() {
					@Override
					public void run() {
						if (participantImpl.getParticipantListener() != null) {
							participantImpl.getParticipantListener().onAudioTrackRemoved(ConversationImpl.this, participantImpl, audioTrack);
						}
					}
				});
			}
		}
	}

	@Override
	public void onAudioTrackStateChanged(final TrackInfo trackInfo) {
		logger.i("onAudioTrackStateChanged " + trackInfo.getParticipantIdentity() + " " + trackInfo.getTrackId());
		if(trackInfo.getTrackOrigin() == TrackOrigin.LOCAL) {
			return;
		} else {
			final ParticipantImpl participantImpl = findOrCreateParticipant(trackInfo.getParticipantIdentity(), null);
			List<AudioTrack> audioTracks = participantImpl.getMediaImpl().getAudioTracks();
			for(final AudioTrack audioTrack: audioTracks) {
				if(trackInfo.getTrackId().equals(audioTrack.getTrackId())) {
					((AudioTrackImpl)audioTrack).updateTrackInfo(trackInfo);
					final Handler participantHandler = participantImpl.getHandler();
					if(participantHandler != null) {
						participantHandler.post(new Runnable() {
							@Override
							public void run() {
								if (participantImpl.getParticipantListener() != null) {
									if(trackInfo.isEnabled()) {
										participantImpl.getParticipantListener().onTrackEnabled(ConversationImpl.this, participantImpl, audioTrack);
									} else {
										participantImpl.getParticipantListener().onTrackDisabled(ConversationImpl.this, participantImpl, audioTrack);
									}
								}
							}
						});
					}
					break;
				}
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
			freeNativeHandle(nativeHandle);
			nativeHandle = 0;
		}
		isDisposed = true;
	}

	public void setLocalMedia(LocalMedia media) {
		checkDisposed();
		localMediaImpl = (LocalMediaImpl)media;
		localMediaImpl.setConversation(this);
	}
	
	void setupExternalCapturer()  {
		try {
			LocalVideoTrack localVideoTrack = localMediaImpl.getLocalVideoTracks().get(0);
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
	public void start(CoreSessionMediaConstraints mediaConstraints) {
		logger.d("starting call");
	
		// TODO: Call only when video is enabled
		setupExternalCapturer();

		start(getNativeHandle(),
				mediaConstraints.isAudioEnabled(),
				mediaConstraints.isAudioMuted(),
				mediaConstraints.isVideoEnabled(),
				mediaConstraints.isVideoPaused());

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
		String[] participantIdentityArray =
				participants.toArray(new String[participants.size()]);
		inviteParticipants(getNativeHandle(), participantIdentityArray);
	}

	@Override
	public boolean enableAudio(boolean enabled, boolean muted) {
		return enableAudio(getNativeHandle(), enabled, muted);
	}

	private synchronized void checkDisposed() {
		if (isDisposed || nativeHandle == 0) {
			throw new IllegalStateException(DISPOSE_MESSAGE);
		}
	}
	
	private native long wrapOutgoingSession(long nativeEndpoint, long nativeSessionObserver, String[] participants);
	private native void start(long nativeSession, boolean enableAudio, boolean muteAudio, boolean enableVideo, boolean pauseVideo);
	private native void setExternalCapturer(long nativeSession, long nativeCapturer);
	private native void stop(long nativeSession);
	private native void setSessionObserver(long nativeSession, long nativeSessionObserver);
	private native void freeNativeHandle(long nativeHandle);
	private native boolean enableVideo(long nativeHandle, boolean enabled, boolean paused);
	private native boolean mute(long nativeSession, boolean on);
	private native boolean isMuted(long nativeSession);
	private native void inviteParticipants(long nativeHandle, String[] participants);
	private native String getConversationSid(long nativeHandle);
	private native boolean enableAudio(long nativeHandle, boolean enabled, boolean muted);

}
