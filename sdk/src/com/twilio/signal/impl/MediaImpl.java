package com.twilio.signal.impl;

import java.util.ArrayList;
import java.util.List;

import com.twilio.signal.AudioTrack;
import com.twilio.signal.Media;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.core.TrackInfo;


public class MediaImpl implements Media {
	private List<VideoTrackImpl> videoTracksImpl = new ArrayList<VideoTrackImpl>();
	private List<AudioTrackImpl> audioTracksImpl = new ArrayList<AudioTrackImpl>();

	@Override
	public List<VideoTrack> getVideoTracks() {
		return new ArrayList<VideoTrack>(videoTracksImpl);
	}

	@Override
	public List<AudioTrack> getAudioTracks() {
		return new ArrayList<AudioTrack>(audioTracksImpl);
	}

	void addVideoTrack(VideoTrackImpl videoTrackImpl) {
		if (videoTrackImpl == null) {
			throw new NullPointerException("VideoTrack can't be null");
		}
		videoTracksImpl.add(videoTrackImpl);
	}

	VideoTrackImpl removeVideoTrack(TrackInfo trackInfo) {
		for(VideoTrackImpl videoTrackImpl : new ArrayList<VideoTrackImpl>(videoTracksImpl)) {
			if(trackInfo.getTrackId().equals(videoTrackImpl.getTrackInfo().getTrackId())) {
				videoTracksImpl.remove(videoTrackImpl);
				return videoTrackImpl;
			}
		}
		return null;
	}

	void addAudioTrack(AudioTrackImpl audioTrackImpl) {
		if (audioTrackImpl == null) {
			throw new NullPointerException("AudioTrack can't be null");
		}
		audioTracksImpl.add(audioTrackImpl);
	}

	AudioTrackImpl removeAudioTrack(TrackInfo trackInfo) {
		for(AudioTrackImpl audioTrackImpl : new ArrayList<AudioTrackImpl>(audioTracksImpl)) {
			if(trackInfo.getTrackId().equals(audioTrackImpl.getTrackInfo().getTrackId())) {
				audioTracksImpl.remove(audioTrackImpl);
				return audioTrackImpl;
			}
		}
		return null;
	}

}
