package com.twilio.video;

import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides video and audio tracks associated with a {@link Participant}
 */
public class Media {
    private List<VideoTrack> videoTracks = new ArrayList<>();
    private List<AudioTrack> audioTracks = new ArrayList<>();

    public AudioTrack getAudioTrack(String trackId) {
        return null;
    }

    public VideoTrack getVideoTrack(String trackId) {
        return null;
    }

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

    public interface Listener {
        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track added to this room
         */
        void onAudioTrackAdded(Media media,
                               AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param audioTrack The audio track removed from this room
         */
        void onAudioTrackRemoved(Media media,
                                 AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * a {@link VideoTrack} to this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track provided by this room
         */
        void onVideoTrackAdded(Media media,
                               VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * a {@link VideoTrack} from this {@link Room}
         *
         * @param room The room associated with this video track
         * @param participant The participant associated with this video track
         * @param videoTrack The video track removed from this room
         */
        void onVideoTrackRemoved(Media media,
                                 VideoTrack videoTrack);


        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been enabled
         *
         * @param room The room associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track enabled in this room
         */
        void onAudioTrackEnabled(Media media, AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} media track
         * has been disabled
         *
         * @param room The room associated with this media track
         * @param participant The participant associated with this media track
         * @param mediaTrack The media track disabled in this room
         */
        void onAudioTrackDisabled(Media media, AudioTrack audioTrack);

        void onVideoTrackEnabled(Media media, VideoTrack videoTrack);
        void onVideoTrackDisabled(Media media, VideoTrack videoTrack);
    }
}
