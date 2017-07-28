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

package com.twilio.video.util;

import org.junit.Assert;

public class VideoAssert extends Assert {
    private static final int DEFAULT_FRAMES_RENDERED_RETRY_MAX = 3;
    private static final String TRACK_SID_REGEX = "^MT[a-zA-Z0-9]{32}$";
    private static final String PARTICIPANT_SID_REGEX = "^PA[a-zA-Z0-9]{32}$";

    public static void assertIsParticipantSid(String participantSid) {
        assertTrue(String.format("%s is not participant sid", participantSid),
                participantSid.matches(PARTICIPANT_SID_REGEX));
    }

    public static void assertIsTrackSid(String trackSid) {
        assertTrue(String.format("%s is not track sid", trackSid),
                trackSid.matches(TRACK_SID_REGEX));
    }

    public static void assertFramesRendered(FrameCountRenderer frameCountRenderer, int timeoutMs)
            throws InterruptedException {
        if (!frameCountRenderer.waitForFrame(timeoutMs)) {
            fail(String.format("Did not render frames after %s milliseconds", timeoutMs));
        }
    }

    public static void assertNoFramesRendered(FrameCountRenderer frameCountRenderer,
                                              int timeoutMs) throws InterruptedException {
        assertNoFramesRendered(frameCountRenderer, timeoutMs, DEFAULT_FRAMES_RENDERED_RETRY_MAX);
    }

    public static void assertNoFramesRendered(FrameCountRenderer frameCountRenderer,
                                              int timeoutMs,
                                              int maxRetries) throws InterruptedException {
        int retries = 0;
        while (frameCountRenderer.waitForFrame(timeoutMs)) {
            if (retries == maxRetries) {
                fail("Rendered frames after expecting not to receive frames");
            }
            retries++;
        }
    }
}
