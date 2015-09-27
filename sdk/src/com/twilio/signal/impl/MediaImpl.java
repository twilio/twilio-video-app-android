package com.twilio.signal.impl;

import java.util.List;
import java.util.ArrayList;

import com.twilio.signal.Media;
import com.twilio.signal.VideoTrack;


public class MediaImpl implements Media {
	List<VideoTrack> videoTracks = new ArrayList<VideoTrack>();	

	@Override
	public List<VideoTrack> getVideoTracks() {
		return videoTracks;
	}

	public void addVideoTrack(VideoTrack videoTrack) {
		videoTracks.add(videoTrack);
	}

	public void removeVideoTrack(VideoTrack videoTrack) {
		videoTracks.remove(videoTrack);
	}
} 
