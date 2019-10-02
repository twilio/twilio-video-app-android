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

/** A published video track represents a video track that has been shared with a {@link Room}. */
public interface VideoTrackPublication extends TrackPublication {
    /**
     * {@link LocalVideoTrackPublication} and {@link RemoteVideoTrackPublication} extend {@link
     * VideoTrackPublication} and each interface implements getVideoTrack with different nullability
     * behavior. {@link LocalVideoTrackPublication#getVideoTrack} is annotated as @NonNull and
     * {@link RemoteVideoTrackPublication#getVideoTrack} is annotated as @Nullable.
     *
     * <p>This method is marked as @Nullable because at least one of the subclasses returns null.
     *
     * @see LocalVideoTrackPublication
     * @see RemoteVideoTrackPublication
     * @return the published video track.
     */
    @Nullable
    VideoTrack getVideoTrack();
}
