package com.twilio.signal.impl;

import java.util.ArrayList;
import java.util.List;

import com.twilio.signal.I420Frame;
import com.twilio.signal.VideoRenderer;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.core.TrackInfo;

public class VideoTrackImpl implements VideoTrack {

	private org.webrtc.VideoTrack videoTrack;
	private TrackInfo trackInfo;
	private List<VideoRenderer> videoRenderers = new ArrayList<VideoRenderer>();

	VideoTrackImpl() {}
	
	VideoTrackImpl(org.webrtc.VideoTrack videoTrack, TrackInfo trackInfo) {
		this.videoTrack = videoTrack;
		this.trackInfo = trackInfo;
	}
	
	void setWebrtcVideoTrack(org.webrtc.VideoTrack videoTrack) {
		this.videoTrack = videoTrack;
	}
	
	void setTrackInfo(TrackInfo trackInfo) {
		this.trackInfo = trackInfo;
	}
	
	org.webrtc.VideoTrack getWebrtcVideoTrack() {
		return videoTrack;
	}

	public TrackInfo getTrackInfo() {
		return trackInfo;
	}

	@Override
	public void addRenderer(VideoRenderer videoRenderer) {
		videoRenderers.add(videoRenderer);
		videoTrack.addRenderer(createWebRtcVideoRenderer(videoRenderer));
	}

	@Override
	public void removeRenderer(VideoRenderer videoRenderer) {
		videoRenderers.remove(videoRenderer);
		videoTrack.removeRenderer(createWebRtcVideoRenderer(videoRenderer));
	}

	@Override
	public List<VideoRenderer> getRenderers() {
		return videoRenderers;
	}
	
	void dispose() {
		if (videoTrack != null) {
			//videoTrack.dispose();
			videoTrack = null;
		}
	}
	
	void invalidateRenderers() {
		for (VideoRenderer renderer : new ArrayList<VideoRenderer>(videoRenderers) ) {
			videoTrack.removeRenderer(createWebRtcVideoRenderer(renderer));
		}
		videoRenderers.clear();
	}
	
	private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
		return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
	}

	private class VideoRendererCallbackAdapter implements org.webrtc.VideoRenderer.Callbacks {
		private VideoRenderer videoRenderer;
		private int width = 0;
		private int height = 0;
	
		public VideoRendererCallbackAdapter(VideoRenderer videoRenderer) {
			this.videoRenderer = videoRenderer;
		}

		@Override
		public boolean canApplyRotation() {
			return false;
		}

		@Override
		public void renderFrame(org.webrtc.VideoRenderer.I420Frame frame) {
			if(width != frame.width || height != frame.height) {
				// Update size
				width = frame.width;
				height = frame.height;
				videoRenderer.setSize(width, height);
			}

			videoRenderer.renderFrame(new I420Frame(frame.width, frame.height, frame.rotationDegree, frame.yuvStrides, frame.yuvPlanes));
		}

	}

}
