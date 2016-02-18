package com.twilio.conversations.impl.core;

public class CoreSessionMediaConstraints {
	private boolean enableAudio;
	private boolean muteAudio;
	private boolean enableVideo;
	private boolean pauseVideo;

	public CoreSessionMediaConstraints(
			boolean enableAudio, boolean muteAudio,
			boolean enableVideo, boolean pauseVideo) {
		this.enableAudio = enableAudio;
		this.muteAudio = muteAudio;
		this.enableVideo = enableVideo;
		this.pauseVideo = pauseVideo;
	}

	public boolean isAudioEnabled() {
		return enableAudio;
	}

	public boolean isAudioMuted() {
		return muteAudio;
	}

	public boolean isVideoEnabled() {
		return enableVideo;
	}

	public boolean isVideoPaused() {
		return pauseVideo;
	}
}
