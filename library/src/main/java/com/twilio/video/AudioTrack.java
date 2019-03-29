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

import android.support.annotation.NonNull;
import java.util.HashMap;

public abstract class AudioTrack implements Track {
    private static final Logger logger = Logger.getLogger(AudioTrack.class);

    private long nativeAudioTrackHandle;
    private final String name;
    private boolean isEnabled;
    protected final HashMap<AudioSink, AudioSinkProxy> audioSinks;

    AudioTrack(long nativeAudioTrackHandle, boolean isEnabled, @NonNull String name) {
        this.nativeAudioTrackHandle = nativeAudioTrackHandle;
        this.isEnabled = isEnabled;
        this.name = name;
        this.audioSinks = new HashMap<>();
    }

    /**
     * Check if this audio track is enabled.
     *
     * @return true if track is enabled.
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Returns the audio track name. A pseudo random string is returned if no track name was
     * specified.
     */
    @NonNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Adds a sink to the Track. Sinks consume raw audio samples for further processing or storage.
     */
    public synchronized void addSink(@NonNull AudioSink audioSink) {
        Preconditions.checkNotNull(audioSink);
        if (!isReleased()) {
            if (!audioSinks.containsKey(audioSink)) {
                AudioSinkProxy proxy = new AudioSinkProxy(audioSink);
                audioSinks.put(audioSink, proxy);
                nativeAddSink(nativeAudioTrackHandle, proxy);
            }
        } else {
            logger.w("Cannot add sink to released audio track");
        }
    }

    /**
     * Removes a sink from the Track.
     *
     * @param audioSink An object that implements the `AudioSink` interface.
     */
    public synchronized void removeSink(@NonNull AudioSink audioSink) {
        Preconditions.checkNotNull(audioSink);
        if (!isReleased()) {
            AudioSinkProxy proxy;
            if ((proxy = audioSinks.get(audioSink)) != null) {
                nativeRemoveSink(nativeAudioTrackHandle, proxy);
                audioSinks.remove(audioSink);
                proxy.release();
            }
        } else {
            logger.w("Cannot remove sink from released audio track");
        }
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    // Require subclasses implement release logic
    abstract void release();

    abstract boolean isReleased();

    private native void nativeAddSink(long nativeAudioTrackHandle, AudioSink audioSink);

    private native void nativeRemoveSink(long nativeAudioTrackHandle, AudioSink audioSink);
}
