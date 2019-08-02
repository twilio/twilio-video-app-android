/*
 * Copyright (C) 2018 Twilio, Inc.
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
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

/*
 * Provides options for creating a test MediaFactory instance. Useful for simulating media scenarios
 * on a device.
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class MediaOptions {
    /*
     * Fields are read from native media factory layer.
     */

    @SuppressWarnings("unused")
    private final boolean enableH264;

    @SuppressWarnings("unused")
    @Nullable
    private final String audioFilePath;

    private MediaOptions(Builder builder) {
        this.enableH264 = builder.enableH264;
        this.audioFilePath = builder.audioFilePath;
    }

    static class Builder {
        private boolean enableH264;
        private @Nullable String audioFilePath;

        Builder enableH264(boolean enableH264) {
            this.enableH264 = enableH264;
            return this;
        }

        /*
         * Provide a path to an audio file. Providing an audio file path configures the
         * MediaFactory instance to use an audio device module that captures audio from the
         * given file.
         */
        Builder audioFilePath(@NonNull String audioFilePath) {
            Preconditions.checkNotNull("audioFilePath should not be null", audioFilePath);
            this.audioFilePath = audioFilePath;
            return this;
        }

        MediaOptions build() {
            return new MediaOptions(this);
        }
    }
}
