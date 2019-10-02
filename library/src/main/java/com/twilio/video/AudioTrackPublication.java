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

import android.support.annotation.Nullable;

/** A published audio track represents an audio track that has been shared with a {@link Room}. */
public interface AudioTrackPublication extends TrackPublication {
    /**
     * {@link LocalAudioTrackPublication} and {@link RemoteAudioTrackPublication} extend {@link
     * AudioTrackPublication} and each interface implements getAudioTrack with different nullability
     * behavior. {@link LocalAudioTrackPublication#getAudioTrack} is annotated as @NonNull and
     * {@link RemoteAudioTrackPublication#getAudioTrack} is annotated as @Nullable.
     *
     * <p>This method is marked as @Nullable because at least one of the subclasses returns null.
     *
     * @see LocalAudioTrackPublication
     * @see RemoteAudioTrackPublication
     * @return the published audio track.
     */
    @Nullable
    AudioTrack getAudioTrack();
}
