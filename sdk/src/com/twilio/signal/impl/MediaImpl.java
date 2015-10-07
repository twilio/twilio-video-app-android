package com.twilio.signal.impl;

import java.util.List;
import java.util.ArrayList;

import com.twilio.signal.Media;
import com.twilio.signal.VideoTrack;
import com.twilio.signal.impl.VideoTrackImpl;
import com.twilio.signal.impl.core.TrackInfo;


public class MediaImpl implements Media {
	List<VideoTrackImpl> videoTracksImpl = new ArrayList<VideoTrackImpl>();	

	@Override
	public List<VideoTrack> getVideoTracks() {
		return new ArrayList<VideoTrack>(videoTracksImpl);
	}

	public void addVideoTrack(VideoTrackImpl videoTrackImpl) {
		videoTracksImpl.add(videoTrackImpl);
	}

	public void removeVideoTrack(VideoTrackImpl videoTrackImpl) {
		videoTracksImpl.remove(videoTrackImpl);
	}

	public VideoTrackImpl removeVideoTrack(TrackInfo trackInfo) {
		for(VideoTrackImpl videoTrackImpl : new ArrayList<VideoTrackImpl>(videoTracksImpl)) {
			if(trackInfo.getTrackId().equals(videoTrackImpl.getTrackInfo().getTrackId())) {
				videoTracksImpl.remove(videoTrackImpl);
				return videoTrackImpl;
			}
		}
		return null;
	}
} 
