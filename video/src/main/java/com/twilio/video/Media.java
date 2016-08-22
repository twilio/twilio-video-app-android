package com.twilio.video;

import android.os.Handler;
import android.provider.MediaStore;

import com.twilio.video.internal.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides video and audio tracks associated with a {@link Participant}
 */
public class Media {

    public interface Listener {
        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param media The media object associated with this audio track
         * @param audioTrack The audio track added to this room
         */
        void onAudioTrackAdded(Media media,
                               AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}
         *
         * @param media The media object associated with this audio track
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

    private Map<String, VideoTrack> videoTrackMap = new HashMap<>();
    private Map<String, AudioTrack> audioTrackMap = new HashMap<>();

    private long nativeMediaContext;
    private InternalMediaListenerImpl internalMediaListenerImpl;
    private InternalMediaListenerHandle internalMediaListenerHandle;
    private Listener listener;
    private Handler handler;

    Media(long nativeMediaContext,
          List<AudioTrack> audioTracks, List<VideoTrack> videoTracks, Handler handler) {
        this.nativeMediaContext = nativeMediaContext;
        this.handler = handler;
        addAudioTracks(audioTracks);
        addVideoTracks(videoTracks);
        internalMediaListenerImpl = new InternalMediaListenerImpl();
        internalMediaListenerHandle =
                new InternalMediaListenerHandle(internalMediaListenerImpl);
        nativeSetInternalListener(nativeMediaContext, internalMediaListenerHandle.get());
    }

    public AudioTrack getAudioTrack(String trackId) {
        return audioTrackMap.get(trackId);
    }

    public VideoTrack getVideoTrack(String trackId) {
        return videoTrackMap.get(trackId);
    }

    /**
     * Retrieves the list of video tracks
     *
     * @return list of video tracks
     */
    public List<VideoTrack> getVideoTracks() {
        return new ArrayList<>(videoTrackMap.values());
    }

    /**
     * Retrieves the list of audio tracks
     *
     * @return list of audio tracks
     */
    public List<AudioTrack> getAudioTracks() {
        return new ArrayList<>(audioTrackMap.values());
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    void addVideoTracks(List<VideoTrack> videoTrackList) {
        for (VideoTrack videoTrack : videoTrackList) {
            if (videoTrack != null && videoTrack.getTrackId() != null) {
                videoTrackMap.put(videoTrack.getTrackId(), videoTrack);
            }
        }
    }

    void addAudioTracks(List<AudioTrack> audioTrackList) {
        for (AudioTrack audioTrack : audioTrackList) {
            if (audioTrack != null && audioTrack.getTrackId() != null) {
                audioTrackMap.put(audioTrack.getTrackId(), audioTrack);
            }
        }

    }

    synchronized void release() {
        // Release all tracks
        for (AudioTrack audioTrack : audioTrackMap.values()) {
            audioTrack.release();
        }
        audioTrackMap.clear();
        for (VideoTrack videoTrack : videoTrackMap.values()) {
            videoTrack.release();
        }
        videoTrackMap.clear();
        if (nativeMediaContext != 0) {
            nativeRelease(nativeMediaContext);
            nativeMediaContext = 0;
            if (internalMediaListenerHandle != null) {
                internalMediaListenerHandle.release();
            }
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
        public void onAudioTrackAdded(final AudioTrack audioTrack) {
            logger.d("onAudioTrackAdded");
            if (listener == null) {
                return;
            }
            if (audioTrack == null) {
                logger.w("Received audio track added callback for non-existing audio track");
            }
            audioTrackMap.put(audioTrack.getTrackId(), audioTrack);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onAudioTrackAdded(Media.this, audioTrack);
                }
            });

        }

        @Override
        public void onAudioTrackRemoved(String trackId) {
            logger.d("onAudioTrackRemoved");
            if (listener == null) {
                return;
            }
            final AudioTrack audioTrack = audioTrackMap.remove(trackId);
            if (audioTrack == null) {
                logger.w("Received audio track removed callback for non-existent audio track");
                return;
            }
            audioTrack.release();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onAudioTrackRemoved(Media.this, audioTrack);
                }
            });

        }

        @Override
        public void onVideoTrackAdded(final VideoTrack videoTrack) {
            logger.d("onVideoTrackAdded");
            if (listener == null) {
                return;
            }
            if (videoTrack == null) {
                logger.w("Received video track added callback for non-existing video track");
            }
            videoTrackMap.put(videoTrack.getTrackId(), videoTrack);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onVideoTrackAdded(Media.this, videoTrack);
                }
            });
        }

        @Override
        public void onVideoTrackRemoved(String trackId) {
            logger.d("onVideoTrackRemoved");
            if (listener == null) {
                return;
            }
            final VideoTrack videoTrack = videoTrackMap.remove(trackId);
            if (videoTrack == null) {
                logger.w("Received video track removed callback for non-existent video track");
                return;
            }
            videoTrack.release();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onVideoTrackRemoved(Media.this, videoTrack);
                }
            });
        }

        @Override
        public void onAudioTrackEnabled(String trackId) {
            logger.d("onAudioTrackEnabled");
            if (listener == null) {
                return;
            }
            final AudioTrack audioTrack = audioTrackMap.get(trackId);
            if (audioTrack == null) {
                logger.w("Received audio track enabled callback for non-existent audio track");
                return;
            }
            audioTrack.setEnabled(true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onAudioTrackEnabled(Media.this, audioTrack);
                }
            });
        }

        @Override
        public void onAudioTrackDisabled(String trackId) {
            logger.d("onAudioTrackDisabled");
            if (listener == null) {
                return;
            }
            final AudioTrack audioTrack = audioTrackMap.get(trackId);
            if (audioTrack == null) {
                logger.w("Received audio track disabled callback for non-existent audio track");
                return;
            }
            audioTrack.setEnabled(false);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onAudioTrackDisabled(Media.this, audioTrack);
                }
            });
        }

        @Override
        public void onVideoTrackEnabled(String trackId) {
            logger.d("onVideoTrackEnabled");
            if (listener == null) {
                return;
            }
            final VideoTrack videoTrack = videoTrackMap.get(trackId);
            if (videoTrack == null) {
                logger.w("Received video track enabled callback for non-existent video track");
                return;
            }
            videoTrack.setEnabled(true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onVideoTrackEnabled(Media.this, videoTrack);
                }
            });
        }

        @Override
        public void onVideoTrackDisabled(String trackId) {
            logger.d("onVideoTrackDisabled");
            if (listener == null) {
                return;
            }
            final VideoTrack videoTrack = videoTrackMap.get(trackId);
            if (videoTrack == null) {
                logger.w("Received video track disabled callback for non-existent video track");
                return;
            }
            videoTrack.setEnabled(false);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Media.this.listener.onVideoTrackDisabled(Media.this, videoTrack);
                }
            });
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
    private native void nativeRelease(long nativeMediaContext);
}
