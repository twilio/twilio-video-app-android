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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

/**
 * A data track represents a unidirectional data source that can be used to send messages to
 * participants of a {@link Room}.
 */
public class LocalDataTrack extends DataTrack {
    private static final Logger logger = Logger.getLogger(LocalDataTrack.class);

    private long nativeLocalDataTrackHandle;
    private final String trackId;
    private final MediaFactory mediaFactory;

    /**
     * Creates a local data track with no name and
     * {@link DataTrackOptions#DEFAULT_DATA_TRACK_OPTIONS}.
     *
     * @param context application context.
     * @return local data track
     */
    public static LocalDataTrack create(@NonNull Context context) {
        return create(context, DataTrackOptions.DEFAULT_DATA_TRACK_OPTIONS, null);
    }

    /**
     * Creates a local data track with no name and provided data track options.
     *
     * @param context application context.
     * @param dataTrackOptions data track options.
     * @return local data track.
     */
    public static LocalDataTrack create(@NonNull Context context,
                                        @Nullable DataTrackOptions dataTrackOptions) {
        return create(context, dataTrackOptions, null);
    }

    /**
     * Creates a local data track with provided name and
     * {@link DataTrackOptions#DEFAULT_DATA_TRACK_OPTIONS}.
     *
     * @param context application context.
     * @param name data track name
     * @return local data track.
     */
    public static LocalDataTrack create(@NonNull Context context,
                                        @Nullable String name) {
        return create(context, DataTrackOptions.DEFAULT_DATA_TRACK_OPTIONS, name);
    }

    /**
     * Creates a local data track with provided name and data track options.
     *
     * @param context application context.
     * @param dataTrackOptions data track options.
     * @param name data track name.
     * @return local data track.
     */
    public static LocalDataTrack create(@NonNull Context context,
                                                  @Nullable DataTrackOptions dataTrackOptions,
                                                  @Nullable String name) {
        Preconditions.checkNotNull(context, "Context must not be null");
        dataTrackOptions = dataTrackOptions == null ?
                (DataTrackOptions.DEFAULT_DATA_TRACK_OPTIONS) :
                dataTrackOptions;

        return MediaFactory.instance(context)
                .createDataTrack(dataTrackOptions.ordered,
                        dataTrackOptions.maxPacketLifeTime,
                        dataTrackOptions.maxRetransmits,
                        name);
    }

    /**
     * Send binary message. The maximum amount of data that can be sent cross-platform in a single
     * invocation of send() is 16KiB. You could possibly send more when both the sender and the
     * receiver are on the same platform.
     *
     * @param messageBuffer binary message
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Using_data_channels#Understanding_message_size_limits">Understanding Message Size Limits</a>
     */
    public synchronized void send(@NonNull ByteBuffer messageBuffer) {
        Preconditions.checkState(!isReleased(), "Cannot send message after data track is released");
        Preconditions.checkNotNull(messageBuffer, "Message buffer must not be null");
        nativeBufferSend(nativeLocalDataTrackHandle, messageBuffer.hasArray() ?
                messageBuffer.array() :
                getMessageByteArray(messageBuffer));
    }

    /**
     * Send string message. The maximum amount of data that can be sent cross-platform in a single
     * invocation of send() is 16KiB. You could possibly send more when both the sender and the
     * receiver are on the same platform.
     *
     * @param message string message.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API/Using_data_channels#Understanding_message_size_limits">Understanding Message Size Limits</a>
     */
    public synchronized void send(@NonNull String message) {
        Preconditions.checkState(!isReleased(), "Cannot send message after data track is released");
        Preconditions.checkNotNull(message, "Message buffer must not be null");
        nativeStringSend(nativeLocalDataTrackHandle, message);
    }

    /**
     * This data track id.
     *
     * @return track id.
     */
    public String getTrackId() {
        return trackId;
    }

    @Override
    public synchronized boolean isEnabled() {
        if (!isReleased()) {
            return super.isEnabled();
        } else {
            logger.e("Local data track is not enabled because it has been released");
            return false;
        }
    }

    /**
     * Returns the local data track name. {@link #trackId} is returned if no name was specified.
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Releases native memory owned by data track.
     */
    public synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeLocalDataTrackHandle);
            nativeLocalDataTrackHandle = 0;
            mediaFactory.release();
        }
    }

    LocalDataTrack(long nativeLocalDataTrackHandle,
                   boolean enabled,
                   boolean ordered,
                   boolean reliable,
                   int maxPacketLifeTime,
                   int maxRetransmits,
                   String trackId,
                   String name,
                   MediaFactory mediaFactory) {
        super(enabled, ordered, reliable, maxPacketLifeTime, maxRetransmits, name);
        this.nativeLocalDataTrackHandle = nativeLocalDataTrackHandle;
        this.trackId = trackId;
        this.mediaFactory = mediaFactory;
    }

    boolean isReleased() {
        return nativeLocalDataTrackHandle == 0;
    }

    /*
     * Called by LocalParticipant at JNI level
     */
    @SuppressWarnings("unused")
    synchronized long getNativeHandle() {
        return nativeLocalDataTrackHandle;
    }

    private byte[] getMessageByteArray(ByteBuffer messageBuffer) {
        byte[] messageByteArray = new byte[messageBuffer.capacity()];
        messageBuffer.get(messageByteArray);

        return messageByteArray;
    }

    private native void nativeBufferSend(long nativeLocalDataTrackHandle, byte[] messageBuffer);
    private native void nativeStringSend(long nativeLocalDataTrackHandle, String message);
    private native void nativeRelease(long nativeLocalVideoTrackHandle);
}
