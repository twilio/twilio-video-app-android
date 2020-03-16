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

/** The verbosity level of Network Quality information for a {@link Participant}. */
public enum NetworkQualityVerbosity {
    /**
     * Nothing is reported for the {@link Participant}. This is not a valid option for the {@link
     * LocalParticipant}.
     */
    NETWORK_QUALITY_VERBOSITY_NONE,
    /** Reports only the {@link NetworkQualityLevel} for the {@link Participant}. */
    NETWORK_QUALITY_VERBOSITY_MINIMAL
}
