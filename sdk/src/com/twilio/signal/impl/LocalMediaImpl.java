package com.twilio.signal.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.CameraCapturerImpl;
import com.twilio.signal.impl.LocalVideoTrackImpl;
import com.twilio.signal.impl.core.TrackInfo;
import com.twilio.signal.impl.logging.Logger;

public class LocalMediaImpl implements LocalMedia {
	
	private ViewGroup container;
	private List<LocalVideoTrackImpl> videoTracksImpl = new ArrayList<LocalVideoTrackImpl>();
	private WeakReference<ConversationImpl> convWeak;
	
	private static int MAX_LOCAL_VIDEO_TRACKS = 1;
	
	private static String TAG = "LocalMediaImpl";
	static final Logger logger = Logger.getLogger(LocalMediaImpl.class);
	
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#getContainerView()
	 */
	@Override
	public ViewGroup getContainerView() {
		return container;
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#attachContainerView(android.view.ViewGroup)
	 */
	@Override
	public void attachContainerView(ViewGroup container) {
		this.container = container;
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#mute(boolean)
	 */
	@Override
	public void mute(boolean on) {
		if (convWeak != null && convWeak.get() != null) {
			convWeak.get().mute(on);
		}
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#isMuted()
	 */
	@Override
	public boolean isMuted() {
		return false;
	}

	@Override
	public List<LocalVideoTrack> getLocalVideoTracks() {
		return new ArrayList<LocalVideoTrack>(videoTracksImpl);
	}

	@Override
	public void addLocalVideoTrack(LocalVideoTrack track)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (track == null) {
			throw new NullPointerException("LocalVideoTrack can't be null");
		}
		if (track instanceof LocalVideoTrackImpl) {
			LocalVideoTrackImpl localVideoTrackImpl = (LocalVideoTrackImpl)track;
			if (videoTracksImpl.size() < MAX_LOCAL_VIDEO_TRACKS) {
				videoTracksImpl.add(localVideoTrackImpl);
			} else {
				throw new UnsupportedOperationException("Maximum size " + MAX_LOCAL_VIDEO_TRACKS + " of LocalVideoTracks reached.");
			}
			if ((convWeak != null) &&  (convWeak.get() != null) && (localVideoTrackImpl.getCameraCapturer() != null)) {
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
				// TODO: return enableVideo if true
			}
		} else {
			throw new IllegalArgumentException("Only TwilioSDK LocalVideoTrack implementation is supported");
		}
		
	}
	

	/*
	 * Media interface
	 */
	@Override
	public List<VideoTrack> getVideoTracks() {
		return new ArrayList<VideoTrack>(videoTracksImpl);
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
	
	
}
