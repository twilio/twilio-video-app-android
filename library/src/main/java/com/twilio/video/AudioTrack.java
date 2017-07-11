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

public abstract class AudioTrack implements Track {
    private final String trackId;
    private boolean isEnabled;

    AudioTrack(String trackId, boolean isEnabled) {
        this.trackId = trackId;
        this.isEnabled = isEnabled;
    }

    /**
     * This audio track id.
     *
     * @return track id.
     */
    @Override
    public String getTrackId() {
        return trackId;
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

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
