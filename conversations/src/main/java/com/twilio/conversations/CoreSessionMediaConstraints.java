package com.twilio.conversations;

class CoreSessionMediaConstraints {
	private boolean enableAudio;
	private boolean muteAudio;
	private boolean enableVideo;
	private boolean pauseVideo;
	private IceOptions iceOptions;

	CoreSessionMediaConstraints(
			boolean enableAudio, boolean muteAudio,
			boolean enableVideo, boolean pauseVideo, IceOptions iceOptions) {
		this.enableAudio = enableAudio;
		this.muteAudio = muteAudio;
		this.enableVideo = enableVideo;
		this.pauseVideo = pauseVideo;
		this.iceOptions = iceOptions;
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

    public IceServer[] getIceServersArray() {
        IceServer[] iceArray = null;
        if ((iceOptions != null) && (iceOptions.iceServers != null)) {
            iceArray = new IceServer[iceOptions.iceServers.size()];
            int i = 0;
            for (IceServer iceServer : iceOptions.iceServers) {
                iceArray[i++] = iceServer;
            }
        }
        return iceArray;
    }

    public IceOptions getIceOptions() { return iceOptions; }
}
