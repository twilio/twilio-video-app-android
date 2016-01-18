package com.twilio.conversations.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.conversations.VideoRenderer;
import com.twilio.conversations.VideoTrack;
import com.twilio.conversations.impl.core.TrackInfo;

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

	TrackInfo getTrackInfo() {
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

	private org.webrtc.VideoRenderer createWebRtcVideoRenderer(VideoRenderer videoRenderer) {
		return new org.webrtc.VideoRenderer(new VideoRendererCallbackAdapter(videoRenderer));
	}

	@Override
	public String getTrackId() {
		return trackInfo != null ? trackInfo.getTrackId() : null;
	}

	void updateTrackInfo(TrackInfo trackInfo) {
		this.trackInfo = trackInfo;
	}

	private class VideoRendererCallbackAdapter implements org.webrtc.VideoRenderer.Callbacks {
		private final VideoRenderer videoRenderer;

		public VideoRendererCallbackAdapter(VideoRenderer videoRenderer) {
			this.videoRenderer = videoRenderer;
		}

		@Override
		public void renderFrame(org.webrtc.VideoRenderer.I420Frame frame) {
			videoRenderer.renderFrame(new I420Frame(frame));
		}
	}
}
