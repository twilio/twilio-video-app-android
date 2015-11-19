package com.twilio.signal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.signal.I420Frame;
import com.twilio.signal.VideoRenderer;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.core.TrackInfo;

public class VideoTrackImpl implements VideoTrack {

	private org.webrtc.VideoTrack videoTrack;
	private TrackInfo trackInfo;
	private Map<VideoRenderer, org.webrtc.VideoRenderer> videoRenderersMap =
			new HashMap<VideoRenderer, org.webrtc.VideoRenderer>();
	

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
		org.webrtc.VideoRenderer webrtcVideoRenderer =
				createWebRtcVideoRenderer(videoRenderer);
		videoRenderersMap.put(videoRenderer, webrtcVideoRenderer);
		videoTrack.addRenderer(webrtcVideoRenderer);
	}

	@Override
	public void removeRenderer(VideoRenderer videoRenderer) {
		org.webrtc.VideoRenderer webrtcVideoRenderer =
				videoRenderersMap.remove(videoRenderer);
		if (webrtcVideoRenderer != null) {
			videoTrack.removeRenderer(webrtcVideoRenderer);
		}
	}

	@Override
	public List<VideoRenderer> getRenderers() {
		return new ArrayList<VideoRenderer>(videoRenderersMap.keySet());
	}
	
	void invalidateRenderers() {
		for (VideoRenderer renderer : new ArrayList<VideoRenderer>(videoRenderersMap.keySet()) ) {
			org.webrtc.VideoRenderer webrtcVideoRenderer =
					videoRenderersMap.remove(renderer);
			if (webrtcVideoRenderer != null) {
				videoTrack.removeRenderer(webrtcVideoRenderer);
			}
		}
		videoRenderersMap.clear();
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
