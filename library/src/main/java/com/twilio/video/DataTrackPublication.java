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

/** A published data track represents a data track that has been shared with a {@link Room}. */
public interface DataTrackPublication extends TrackPublication {
    /**
     * {@link LocalDataTrackPublication} and {@link RemoteDataTrackPublication} extend {@link
     * DataTrackPublication} and each interface implements getDataTrack with different nullability
     * behavior. {@link LocalDataTrackPublication#getDataTrack} is annotated as @NonNull and {@link
     * RemoteDataTrackPublication#getDataTrack} is annotated as @Nullable.
     *
     * <p>This method is marked as @Nullable because at least one of the subclasses returns null.
     *
     * @see LocalDataTrackPublication
     * @see RemoteDataTrackPublication
     * @return the published data track.
     */
    @Nullable
    DataTrack getDataTrack();
}
