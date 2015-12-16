package com.twilio.signal.impl;

import com.twilio.signal.AudioTrack;
import com.twilio.signal.impl.core.TrackInfo;

public class AudioTrackImpl implements AudioTrack {

    private org.webrtc.AudioTrack audioTrack;
    private TrackInfo trackInfo;

    AudioTrackImpl() {}

    AudioTrackImpl(org.webrtc.AudioTrack audioTrack, TrackInfo trackInfo) {
        this.audioTrack = audioTrack;
        this.trackInfo = trackInfo;
    }

	TrackInfo getTrackInfo() {
		return trackInfo;
	}

    @Override
    public String getTrackId() {
        return trackInfo != null ? trackInfo.getTrackId() : null;
    }

    void updateTrackInfo(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }
}
