package com.twilio.conversations.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.impl.core.TrackInfo;
import com.twilio.conversations.impl.logging.Logger;
import com.twilio.conversations.impl.util.CallbackHandler;

public class LocalMediaImpl implements LocalMedia {

	private List<LocalVideoTrackImpl> videoTracksImpl = new ArrayList<LocalVideoTrackImpl>();
	private WeakReference<ConversationImpl> convWeak;
	private boolean audioEnabled;
	private boolean audioMuted;
	private Handler handler;
	private LocalMediaListener localMediaListener;

	private static int MAX_LOCAL_VIDEO_TRACKS = 1;

	private static String TAG = "LocalMediaImpl";
	static final Logger logger = Logger.getLogger(LocalMediaImpl.class);

	public LocalMediaImpl(LocalMediaListener localMediaListener) {
		this.localMediaListener = localMediaListener;
		audioEnabled = true;
		audioMuted = false;

		handler = CallbackHandler.create();
		if(handler == null) {
			throw new IllegalThreadStateException("This thread must be able to obtain a Looper");
		}
	}

	Handler getHandler() {
		return handler;
	}

	LocalMediaListener getLocalMediaListener() {
		return localMediaListener;
	}

	@Override
	public boolean mute(boolean on) {
		if (convWeak != null && convWeak.get() != null) {
			audioMuted = on;
			return convWeak.get().mute(on);
		} else if (audioMuted != on){
			audioMuted = on;
			return true;
		}
		return false;
	}

	@Override
	public boolean isMuted() {
		return audioMuted;
	}

	@Override
	public List<LocalVideoTrack> getLocalVideoTracks() {
		return new ArrayList<LocalVideoTrack>(videoTracksImpl);
	}

	@Override
	public boolean addLocalVideoTrack(LocalVideoTrack track)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (track == null) {
			throw new NullPointerException("LocalVideoTrack can't be null");
		}
		if (track instanceof LocalVideoTrackImpl) {
			LocalVideoTrackImpl localVideoTrackImpl = (LocalVideoTrackImpl)track;
			if (videoTracksImpl.size() >= MAX_LOCAL_VIDEO_TRACKS) {
				throw new UnsupportedOperationException("Maximum size " + MAX_LOCAL_VIDEO_TRACKS + " of LocalVideoTracks reached.");
			}
			if (localVideoTrackImpl.getCameraCapturer() == null) {
				throw new IllegalArgumentException("LocalVideoTrack must have a camera capturer associated with the track");
			}
			videoTracksImpl.add(localVideoTrackImpl);
			if ((convWeak != null) &&  (convWeak.get() != null) ) {
				// LocalVideoTrack is added during conversation
				// TODO: we should use localVideoTrackImpl.isCameraEnabled() as second param here,
				// it is hard coded as false for now until we resolve issue with CameraCapturer starting in disabled mode.
				// This leaves responsibility to a user to unpause the capturer, which user doesn't have to do
				// during initial creation. This is inconsistent behavior and it should be more investigated.
				CameraCapturerImpl cameraCapturerImpl = (CameraCapturerImpl)localVideoTrackImpl.getCameraCapturer();
				long nativeVideoCapturer = cameraCapturerImpl.getNativeVideoCapturer();
				if(nativeVideoCapturer == 0) {
					logger.d("Create a new external capturer since the nativeVideoCapturer is no longer valid");
					convWeak.get().setupExternalCapturer();
				}
				boolean enabledVideo = convWeak.get().enableVideo(true, false);
				if(!enabledVideo) {
					// Remove the video track since it failed to be added
					videoTracksImpl.remove(localVideoTrackImpl);
				}
				return enabledVideo;
			} else {
				// The LocalVideoTrack is always added when a conversation is not active
				return true;
			}
		} else {
			throw new IllegalArgumentException("Only TwilioSDK LocalVideoTrack implementation is supported");
		}
	}

	@Override
	public boolean removeLocalVideoTrack(LocalVideoTrack track) throws IllegalArgumentException{
		if (!(track instanceof LocalVideoTrackImpl)) {
			throw new IllegalArgumentException("Only TwilioSDK LocalVideoTrack implementation is supported");
		}
		if (videoTracksImpl.size() == 0) {
			logger.d("LocalVideoTracks list is empty");
			return false;
		} else if (!videoTracksImpl.contains(track)) {
			logger.d("LocalVideoTrack is not found!");
			return false;
		}
		if (convWeak == null || convWeak.get() == null) {
			logger.d("Conversation is null");
			return false;
		}
		ConversationImpl conv = convWeak.get();
		return conv.enableVideo(false, false);
	}

	LocalVideoTrackImpl removeLocalVideoTrack(TrackInfo trackInfo) {
		for(LocalVideoTrackImpl videoTrackImpl : new ArrayList<LocalVideoTrackImpl>(videoTracksImpl)) {
			if(trackInfo.getTrackId().equals(videoTrackImpl.getTrackInfo().getTrackId())) {
				videoTracksImpl.remove(videoTrackImpl);
				return videoTrackImpl;
			}
		}
		return null;
	}

	void setConversation(ConversationImpl conversation) {
		this.convWeak = new WeakReference<ConversationImpl>(conversation);
	}

	@Override
	public synchronized boolean addMicrophone() {
		if (!audioEnabled) {
			audioEnabled = enableAudio(true);
			return audioEnabled;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean removeMicrophone() {
		if (audioEnabled) {
			audioEnabled = !enableAudio(false);
			return !audioEnabled;
		} else {
			return false;
		}
	}

	private boolean enableAudio(boolean enable) {
		if (convWeak != null && convWeak.get() != null) {
			return convWeak.get().enableAudio(enable, false);
		} else {
			// The conversation is not ongoing. Always return true
			return true;
		}
	}

	@Override
	public boolean isMicrophoneAdded() {
		return audioEnabled;
	}

}
