/*
 * Copyright (C) 2020 Twilio, Inc.
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

/**
 * {@link NetworkQualityConfiguration} allows you to specify verbosity levels of Network Quality
 * information returned by the Network Quality API.
 */
public class NetworkQualityConfiguration {
    /** The {@link NetworkQualityVerbosity} for the Local Participant. */
    public final NetworkQualityVerbosity local;

    /** The {@link NetworkQualityVerbosity} for Remote Participants. */
    public final NetworkQualityVerbosity remote;

    /**
     * Creates a {@link NetworkQualityConfiguration} object with the default values, {@link
     * NetworkQualityVerbosity#NETWORK_QUALITY_VERBOSITY_MINIMAL} for the Local Participant and
     * {@link NetworkQualityVerbosity#NETWORK_QUALITY_VERBOSITY_NONE} for the Remote Participants.
     */
    public NetworkQualityConfiguration() {
        this(
                NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE);
    }

    /**
     * Creates a {@link NetworkQualityConfiguration} object with the provided {@link
     * NetworkQualityVerbosity} levels.
     *
     * @param local The {@link NetworkQualityVerbosity} for the Local Participant. {@link
     *     NetworkQualityVerbosity#NETWORK_QUALITY_VERBOSITY_NONE} is invalid for the Local
     *     Participant and will throw an IllegalArgumentException.
     * @param remote The {@link NetworkQualityVerbosity} for the Remote Participants.
     */
    public NetworkQualityConfiguration(
            @NonNull NetworkQualityVerbosity local, @NonNull NetworkQualityVerbosity remote) {
        Preconditions.checkNotNull(local, "Local verbosity cannot be null");
        Preconditions.checkArgument(
                local != NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE,
                "Local verbosity cannot be 'NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE'");
        Preconditions.checkNotNull(remote, "Remote verbosity cannot be null");

        this.local = local;
        this.remote = remote;
    }

    @NonNull
    @Override
    public String toString() {
        return "NetworkQualityConfiguration{local=" + local + ", remote=" + remote + '}';
    }
}
