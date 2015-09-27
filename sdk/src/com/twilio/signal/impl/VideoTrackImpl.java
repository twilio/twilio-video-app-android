package com.twilio.signal.impl;

import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoRenderer;
import com.twilio.signal.I420Frame;

import java.util.List;
import java.util.ArrayList;

public class VideoTrackImpl implements VideoTrack {

	private org.webrtc.VideoTrack videoTrack;
	private List<VideoRenderer> videoRenderers = new ArrayList<VideoRenderer>();

	private VideoTrackImpl(org.webrtc.VideoTrack videoTrack) {
		this.videoTrack = videoTrack;
	}

	public static VideoTrack create(org.webrtc.VideoTrack videoTrack) {
		return new VideoTrackImpl(videoTrack);
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
