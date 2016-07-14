package com.twilio.rooms;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides video and audio tracks associated with a {@link Participant}
 */
public class Media {
    private List<VideoTrack> videoTracks = new ArrayList<>();
    private List<AudioTrack> audioTracks = new ArrayList<>();

    /**
     * Retrieves the list of video tracks
     *
     * @return list of video tracks
     */
    public List<VideoTrack> getVideoTracks() {
        return new ArrayList<>(videoTracks);
    }

    /**
     * Retrieves the list of audio tracks
     *
     * @return list of audio tracks
     */
    public List<AudioTrack> getAudioTracks() {
        return new ArrayList<>(audioTracks);
    }

    void addVideoTrack(VideoTrack videoTrack) {
        if (videoTrack == null) {
            throw new NullPointerException("VideoTrack can't be null");
        }
        videoTracks.add(videoTrack);
    }

    VideoTrack removeVideoTrack(TrackInfo trackInfo) {
        for(VideoTrack videoTrack : new ArrayList<>(videoTracks)) {
            if(trackInfo.getTrackId().equals(videoTrack.getTrackInfo().getTrackId())) {
                videoTracks.remove(videoTrack);
                return videoTrack;
            }
        }
        return null;
    }

    void addAudioTrack(AudioTrack audioTrack) {
        if (audioTrack == null) {
            throw new NullPointerException("AudioTrack can't be null");
        }
        audioTracks.add(audioTrack);
    }

    AudioTrack removeAudioTrack(TrackInfo trackInfo) {
        for(AudioTrack audioTrack : new ArrayList<>(audioTracks)) {
            if(trackInfo.getTrackId().equals(audioTrack.getTrackInfo().getTrackId())) {
                audioTracks.remove(audioTrack);
                return audioTrack;
            }
        }
        return null;
    }
}
