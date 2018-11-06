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

public abstract class AudioTrack implements Track {
    private long nativeAudioTrackHandle;
    private final String name;
    private boolean isEnabled;

    AudioTrack(long nativeAudioTrackHandle, boolean isEnabled, @NonNull String name) {
        this.nativeAudioTrackHandle = nativeAudioTrackHandle;
        this.isEnabled = isEnabled;
        this.name = name;
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
    @Override
    public String getName() {
        return name;
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    // Require subclasses implement release logic
    abstract void release();
}
