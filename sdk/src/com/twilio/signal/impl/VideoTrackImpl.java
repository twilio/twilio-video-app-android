package com.twilio.signal.impl;

import com.twilio.signal.VideoTrack;
import com.twilio.signal.VideoRenderer;

public class VideoTrackImpl implements VideoTrack {

	org.webrtc.VideoTrack _videoTrack;

	private VideoTrackImpl(org.webrtc.VideoTrack _videoTrack) {
		this._videoTrack = _videoTrack;
	}

	public static VideoTrack create(org.webrtc.VideoTrack _videoTrack) {
		return new VideoTrackImpl(_videoTrack);
	}

	public void addRenderer(VideoRenderer videoRenderer) {
		_videoTrack.addRenderer(createWebRtcVideoRenderer(videoRenderer));
	}

	public void removeRenderer(VideoRenderer videoRenderer) {
		_videoTrack.removeRenderer(createWebRtcVideoRenderer(videoRenderer));
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
				// update size
				width = frame.width;
				height = frame.height;
				videoRenderer.setSize(width, height);
			}

			videoRenderer.renderFrame(new I420FrameImpl(frame.height, frame.width, frame.yuvStrides, frame.yuvPlanes));
		}
	
	}

}
