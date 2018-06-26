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
import android.support.annotation.Nullable;

/** A remote data track publication represents a {@link RemoteDataTrack}. */
public class RemoteDataTrackPublication implements DataTrackPublication {
  private final String sid;
  private final String name;
  private RemoteDataTrack remoteDataTrack;
  private boolean subscribed;
  private boolean enabled;

  RemoteDataTrackPublication(
      boolean subscribed, boolean enabled, @NonNull String sid, @NonNull String name) {
    this.enabled = enabled;
    this.subscribed = subscribed;
    this.sid = sid;
    this.name = name;
  }

  /**
   * Returns the remote data track's server identifier. This value uniquely identifies the remote
   * data track within the scope of a {@link Room}.
   */
  @Override
  public String getTrackSid() {
    return sid;
  }

  /**
   * Returns the base data track object of the published remote data track. {@code null} is returned
   * if the track is not subscribed to.
   */
  @Override
  public synchronized @Nullable DataTrack getDataTrack() {
    return remoteDataTrack;
  }

  /**
   * Returns the name of the published data track. An empty string is returned if no track name was
   * specified.
   */
  @Override
  public String getTrackName() {
    return name;
  }

  /** Returns true if the published data track is enabled or false otherwise. */
  @Override
  public boolean isTrackEnabled() {
    return enabled;
  }

  /** Check if the remote data track is subscribed to by the {@link LocalParticipant}. */
  public synchronized boolean isTrackSubscribed() {
    return subscribed;
  }

  /**
   * Returns the published remote data track. {@code null} is returned if the track is not
   * subscribed to.
   */
  public synchronized @Nullable RemoteDataTrack getRemoteDataTrack() {
    return remoteDataTrack;
  }

  /*
   * Set by remote participant listener proxy.
   */
  synchronized void setSubscribed(boolean subscribed) {
    this.subscribed = subscribed;
  }

  /*
   * Called from JNI layer when a track has been subscribed to.
   */
  @SuppressWarnings("unused")
  synchronized void setRemoteDataTrack(RemoteDataTrack remoteDataTrack) {
    this.remoteDataTrack = remoteDataTrack;
  }
}
