/*
 * Copyright (C) 2017 Twilio, Inc.
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

import static android.Manifest.permission.RECORD_AUDIO;

/**
 * Represents a local audio source.
 */
public class LocalAudioTrack extends AudioTrack {
    private static final Logger logger = Logger.getLogger(LocalAudioTrack.class);

    private final String trackId;
    private final MediaFactory mediaFactory;
    private long nativeLocalAudioTrackHandle;

    /**
     * Creates an audio track. Note that the RECORD_AUDIO permission must be granted
     * in order for this operation to succeed. If RECORD_AUDIO is not granted null is returned.
     *
     * @param context application context.
     * @param enabled initial state of audio track.
     * @return local audio track if successfully added or null if audio track could not be created.
     */
    public static LocalAudioTrack create(@NonNull Context context,
                                         boolean enabled) {
        return create(context, enabled, null);
    }

    /**
     * Creates an audio track. Note that the RECORD_AUDIO permission must be granted
     * in order for this operation to succeed. If RECORD_AUDIO is not granted null is returned.
     *
     * @param context applicatoin context.
     * @param enabled initial state of audio track.
     * @param audioOptions audio options to be applied to the track.
     * @return local audio track if successfully added or null if audio track could not be created.
     */
    public static LocalAudioTrack create(@NonNull Context context,
                                         boolean enabled,
                                         @Nullable AudioOptions audioOptions) {
        Preconditions.checkNotNull(context);
        Preconditions.checkState(Util.permissionGranted(context, RECORD_AUDIO), "RECORD_AUDIO " +
                "permission must be granted to create audio track");

        LocalAudioTrack localAudioTrack = MediaFactory.instance(context)
                .createAudioTrack(enabled, audioOptions);

        if (localAudioTrack == null) {
            logger.e("Failed to create local audio track");
        }

        return localAudioTrack;
    }

    /**
     * Check if the local audio track is enabled.
     *
     * When the value is false, the local audio track is muted. When the value is true the
     * local audio track is live.
     *
     * @return true if the local audio is enabled.
     */
    @Override
    public synchronized boolean isEnabled() {
        if (!isReleased()) {
            return nativeIsEnabled(nativeLocalAudioTrackHandle);
        } else {
            logger.w("Local audio track is not enabled because it has been released");

            return false;
        }
    }

    /**
     * This audio track id.
     *
     * @return track id.
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Sets the state of the local audio track. The results of this operation are signaled to other
     * Participants in the same Room. When an audio track is disabled, the audio is muted.
     *
     * @param enable the desired state of the local audio track.
     */
    public synchronized void enable(boolean enable) {
        if (!isReleased()) {
            nativeEnable(nativeLocalAudioTrackHandle, enable);
        } else {
            logger.e("Cannot enable a local audio track that has been removed");
        }
    }

    /**
     * Releases native memory owned by audio track.
     */
    public synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeLocalAudioTrackHandle);
            nativeLocalAudioTrackHandle = 0;
            mediaFactory.release();
        }
    }

    LocalAudioTrack(long nativeLocalAudioTrackHandle,
                    String trackId,
                    boolean enabled,
                    MediaFactory mediaFactory) {
        super(enabled);
        this.trackId = trackId;
        this.nativeLocalAudioTrackHandle = nativeLocalAudioTrackHandle;
        this.mediaFactory = mediaFactory;
    }

    boolean isReleased() {
        return nativeLocalAudioTrackHandle == 0;
    }

    /*
     * Called by LocalParticipant at JNI level.
     */
    @SuppressWarnings("unused")
    synchronized long getNativeHandle() {
        return nativeLocalAudioTrackHandle;
    }

    private native boolean nativeIsEnabled(long nativeLocalAudioTrackHandle);
    private native void nativeEnable(long nativeLocalAudioTrackHandle, boolean enable);
    private native void nativeRelease(long nativeLocalAudioTrackHandle);
}
