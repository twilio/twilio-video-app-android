package com.twilio.video;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A participant represents a remote user that can connect to a {@link Room}.
 */
public class Participant {
    private static final Logger logger = Logger.getLogger(Participant.class);

    private final String identity;
    private final String sid;
    private final List<RemoteAudioTrack> remoteAudioTracks;
    private final List<RemoteVideoTrack> remoteVideoTracks;
    private final Handler handler;

    /*
     * We pass all native participant callbacks through the listener proxy and atomically
     * forward events to the developer listener.
     */
    private final AtomicReference<Listener> listenerReference = new AtomicReference<>(null);
    private final Listener participantListenerProxy = new Listener() {
        @Override
        public void onAudioTrackAdded(final Participant participant,
                                      final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackAdded");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track added callback for non-existing audio track");
                return;
            }
            remoteAudioTracks.add(remoteAudioTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackAdded(participant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackRemoved(final Participant participant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackRemoved");
            remoteAudioTracks.remove(remoteAudioTrack);
            if (remoteAudioTrack == null) {
                logger.w("Received audio track removed callback for non-existent audio track");
                return;
            }
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackRemoved(participant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackAdded(final Participant participant,
                                      final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackAdded");
            if (remoteVideoTrack == null) {
                logger.w("Received video track added callback for non-existing video track");
                return;
            }
            remoteVideoTracks.add(remoteVideoTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackAdded(participant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackRemoved(final Participant participant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackRemoved");
            remoteVideoTracks.remove(remoteVideoTrack);
            if (remoteVideoTrack == null) {
                logger.w("Received video track removed callback for non-existent video track");
                return;
            }
            remoteVideoTrack.release();
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackRemoved(participant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackEnabled(final Participant participant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackEnabled");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track enabled callback for non-existent audio track");
                return;
            }
            remoteAudioTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackEnabled(participant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackDisabled(final Participant participant,
                                         final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackDisabled");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track disabled callback for non-existent audio track");
                return;
            }
            remoteAudioTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackDisabled(participant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackEnabled(final Participant participant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackEnabled");
            if (remoteVideoTrack == null) {
                logger.w("Received video track enabled callback for non-existent video track");
                return;
            }
            remoteVideoTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackEnabled(participant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackDisabled(final Participant participant,
                                         final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackDisabled");
            if (remoteVideoTrack == null) {
                logger.w("Received video track disabled callback for non-existent video track");
                return;
            }
            remoteVideoTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackDisabled(participant, remoteVideoTrack);
                    }
                });
            }
        }
    };
    private long nativeParticipantContext;

    Participant(String identity,
                String sid,
                List<RemoteAudioTrack> remoteAudioTracks,
                List<RemoteVideoTrack> remoteVideoTracks,
                Handler handler,
                long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.remoteAudioTracks = remoteAudioTracks;
        this.remoteVideoTracks = remoteVideoTracks;
        this.handler = handler;
        this.nativeParticipantContext = nativeParticipantContext;
        nativeCreateParticipantListenerProxy(participantListenerProxy, nativeParticipantContext);
    }

    /**
     * Returns the identity of the participant.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the SID of a participant.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Retrieves the list of remote audio tracks.
     *
     * @return list of audio tracks.
     */
    public List<RemoteAudioTrack> getSubscribedAudioTracks() {
        return remoteAudioTracks;
    }

    /**
     * Retrieves the list of remote video tracks.
     *
     * @return list of video tracks.
     */
    public List<RemoteVideoTrack> getSubscribedVideoTracks() {
        return remoteVideoTracks;
    }

    /**
     * Set listener for this participant events.
     *
     * @param listener of participant events.
     */
    public void setListener(Participant.Listener listener) {
        listenerReference.set(listener);
    }

    /**
     * Checks if the participant is connected to a room.
     *
     * @return true if the participant is connected to a room and false if not.
     */
    public synchronized boolean isConnected() {
        if (!isReleased()) {
            return nativeIsConnected(nativeParticipantContext);
        } else {
            return false;
        }
    }

    synchronized void release() {
        if (!isReleased()) {
            for (RemoteVideoTrack remoteVideoTrack : remoteVideoTracks) {
                remoteVideoTrack.release();
            }
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    public interface Listener {
        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link RemoteAudioTrack} to this {@link Room}.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track added to this room.
         */
        void onAudioTrackAdded(Participant participant,
                               RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * an {@link RemoteAudioTrack} from this {@link Room}.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track removed from this room.
         */
        void onAudioTrackRemoved(Participant participant,
                                 RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link RemoteVideoTrack} to this {@link Room}.
         *
         * @param participant The participant object associated with this video track.
         * @param remoteVideoTrack The video track added to this room.
         */
        void onVideoTrackAdded(Participant participant,
                               RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * an {@link RemoteVideoTrack} from this {@link Room}.
         *
         * @param participant The participant object associated with this video track.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onVideoTrackRemoved(Participant participant,
                                 RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link Participant} audio track
         * has been enabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track enabled in this room.
         */
        void onAudioTrackEnabled(Participant participant, RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link Participant} audio track
         * has been disabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track disabled in this room.
         */
        void onAudioTrackDisabled(Participant participant, RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link Participant} video track
         * has been enabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteVideoTrack The video track enabled in this room.
         */
        void onVideoTrackEnabled(Participant participant, RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link Participant} video track
         * has been disabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param remoteVideoTrack The video track disabled in this room.
         */
        void onVideoTrackDisabled(Participant participant, RemoteVideoTrack remoteVideoTrack);
    }

    private native void nativeCreateParticipantListenerProxy(Participant.Listener participantListenerProxy,
                                                             long nativeParticipantContext);
    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
