package com.twilio.video;

import android.provider.MediaStore;

import com.twilio.video.internal.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides video and audio tracks associated with a {@link Participant}
 */
public class Media {

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

    private static final Logger logger = Logger.getLogger(Media.class);

    private List<VideoTrack> videoTracks = new ArrayList<>();
    private List<AudioTrack> audioTracks = new ArrayList<>();

    private long nativeMediaContext;
    private InternalMediaListenerImpl internalMediaListenerImpl;
    private InternalMediaListenerHandle internalMediaListenerHandle;
    private Listener listener;

    Media(long nativeMediaContext, List<AudioTrack> audioTracks, List<VideoTrack> videoTracks) {
        this.nativeMediaContext = nativeMediaContext;
        this.audioTracks = audioTracks;
        this.videoTracks = videoTracks;
        internalMediaListenerImpl = new InternalMediaListenerImpl();
        internalMediaListenerHandle =
                new InternalMediaListenerHandle(internalMediaListenerImpl);
        nativeSetInternalListener(nativeMediaContext, internalMediaListenerHandle.get());
    }

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

    public void setListener(Listener listener) {
        this.listener = listener;
        if (listener == null) {
            internalMediaListenerHandle.release();
            internalMediaListenerHandle = null;
            internalMediaListenerImpl = null;
        } else if (internalMediaListenerImpl == null) {
            // Lazy initialize internal listener
            internalMediaListenerImpl = new InternalMediaListenerImpl();
            internalMediaListenerHandle =
                    new InternalMediaListenerHandle(internalMediaListenerImpl);

        }
    }

    void addVideoTrack(VideoTrack videoTrack) {
        if (videoTrack == null) {
            throw new NullPointerException("VideoTrack can't be null");
        }
        videoTracks.add(videoTrack);
    }

    VideoTrack removeVideoTrack(TrackInfo trackInfo) {
        for(VideoTrack videoTrack : new ArrayList<>(videoTracks)) {
            if(trackInfo.getTrackId().equals(videoTrack.getTrackId())) {
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
            if(trackInfo.getTrackId().equals(audioTrack.getTrackId())) {
                audioTracks.remove(audioTrack);
                return audioTrack;
            }
        }
        return null;
    }

    void release() {
        if (nativeMediaContext != 0) {
            //nativeRelease(nativeMediaContext);
            nativeMediaContext = 0;
            internalMediaListenerHandle.release();
        }
    }

    // JNI Callbacks Interface
    interface InternalMediaListener {
        void onAudioTrackAdded(AudioTrack audioTrack);
        void onAudioTrackRemoved(String trackId);
        void onVideoTrackAdded(VideoTrack videoTrack);
        void onVideoTrackRemoved(String trackId);
        void onAudioTrackEnabled(String trackId);
        void onAudioTrackDisabled(String trackId);
        void onVideoTrackEnabled(String trackId);
        void onVideoTrackDisabled(String trackId);
    }

    class InternalMediaListenerImpl implements InternalMediaListener {

        @Override
        public void onAudioTrackAdded(AudioTrack audioTrack) {
            logger.d("onAudioTrackAdded");
        }

        @Override
        public void onAudioTrackRemoved(String trackId) {
            logger.d("onAudioTrackRemoved");
        }

        @Override
        public void onVideoTrackAdded(VideoTrack videoTrack) {
            logger.d("onVideoTrackAdded");
        }

        @Override
        public void onVideoTrackRemoved(String trackId) {
            logger.d("onVideoTrackRemoved");
        }

        @Override
        public void onAudioTrackEnabled(String trackId) {
            logger.d("onAudioTrackEnabled");
        }

        @Override
        public void onAudioTrackDisabled(String trackId) {
            logger.d("onAudioTrackDisabled");
        }

        @Override
        public void onVideoTrackEnabled(String trackId) {
            logger.d("onVideoTrackEnabled");
        }

        @Override
        public void onVideoTrackDisabled(String trackId) {
            logger.d("onVideoTrackDisabled");
        }
    }

    class InternalMediaListenerHandle extends NativeHandle {


        public InternalMediaListenerHandle(InternalMediaListener listener) {
            super(listener);
        }

        /*
         * Native Handle
         */
        @Override
        protected native long nativeCreate(Object object);

        @Override
        protected native void nativeFree(long nativeHandle);


    }

    private native void nativeSetInternalListener(
            long nativeMediaContext, long nativeInternalListener);
}
