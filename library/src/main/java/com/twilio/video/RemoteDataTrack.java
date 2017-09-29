/*
 * Copyright (C) 2017 Twilio, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.os.Handler;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

/**
 * A remote data track represents a unidirectional remote data source from which messages can
 * be received from a participant.
 */
public class RemoteDataTrack extends DataTrack {
    private static final Logger logger = Logger.getLogger(RemoteDataTrack.class);

    private long nativeRemoteDataTrackContext;
    private Handler handler;

    /*
     * All native participant callbacks are passed through the listener proxy and atomically
     * forward events to the developer listener.
     */
    private Listener listener;

    /*
     * This listener proxy is bound at the JNI level.
     */
    @SuppressWarnings("unused")
    private final Listener dataTrackListenerProxy = new Listener() {

        /*
         * The onMessage callback is synchronized on the RemoteDataTrack instance in the
         * notifier and developer thread to ensure the handler and listener are atomically accessed
         * before notifying the developer.
         */

        @Override
        public void onMessage(final RemoteDataTrack remoteDataTrack,
                              final ByteBuffer messageBuffer) {
            checkCallback(messageBuffer, "onMessage(ByteBuffer)");

            synchronized (RemoteDataTrack.this) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (RemoteDataTrack.this) {
                                logger.d("onMessage(ByteBuffer)");

                                if (listener != null) {
                                    listener.onMessage(remoteDataTrack, messageBuffer);
                                }
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onMessage(final RemoteDataTrack remoteDataTrack, final String message) {
            checkCallback(message, "onMessage(String)");

            synchronized (RemoteDataTrack.this) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (RemoteDataTrack.this) {
                                logger.d("onMessage(String)");

                                if (listener != null) {
                                    listener.onMessage(remoteDataTrack, message);
                                }
                            }
                        }
                    });
                }
            }
        }

        private void checkCallback(Object message, String callback) {
            Preconditions.checkNotNull(message, "Received null message in %s", callback);
        }
    };

    /**
     * Set the remote data track listener. The thread on which this method is called is the
     * same thread used to notify of received data track messages.
     *
     * @param listener data track listener
     */
    public synchronized void setListener(@Nullable Listener listener) {
        this.handler = listener != null ? Util.createCallbackHandler() : null;
        this.listener = listener;
    }

    RemoteDataTrack(boolean enabled,
                    boolean ordered,
                    boolean reliable,
                    int maxPacketLifeTime,
                    int maxRetransmits,
                    String name,
                    long nativeRemoteDataTrackContext) {
        super(enabled, ordered, reliable, maxPacketLifeTime, maxRetransmits, name);
        this.nativeRemoteDataTrackContext = nativeRemoteDataTrackContext;
    }

    synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeRemoteDataTrackContext);
            nativeRemoteDataTrackContext = 0;
        }
    }

    boolean isReleased() {
        return nativeRemoteDataTrackContext == 0;
    }

    /**
     * Interface that provides {@link RemoteDataTrack} events.
     */
    public interface Listener {
        /**
         * This method notifies the listener that a binary message was received.
         *
         * @param remoteDataTrack Remote data track.
         * @param messageBuffer The binary message received.
         */
        void onMessage(RemoteDataTrack remoteDataTrack, ByteBuffer messageBuffer);

        /**
         * This method notifies the listener that a string message was received.
         *
         * @param remoteDataTrack Remote data track.
         * @param message The string message received.
         */
        void onMessage(RemoteDataTrack remoteDataTrack, String message);
    }

    private native void nativeRelease(long nativeRemoteDataTrackContext);
}
