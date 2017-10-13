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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.FrameCountRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class VideoTrackTest {
    private Context context;
    private final FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
    private LocalVideoTrack localVideoTrack;
    private VideoTrack videoTrack;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        localVideoTrack = LocalVideoTrack.create(context, true, new FakeVideoCapturer());
        videoTrack = new InstrumentationTestVideoTrack(localVideoTrack.getWebRtcTrack());
    }

    @After
    public void teardown() {
        videoTrack.release();
        localVideoTrack.release();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldAllowAddRendererAfterReleased() {
        videoTrack.release();
        videoTrack.addRenderer(frameCountRenderer);
    }

    @Test
    public void shouldAllowRemoveRendererAfterReleased() {
        videoTrack.release();
        videoTrack.removeRenderer(frameCountRenderer);
    }

    @Test
    public void release_shouldBeIdempotent() {
        videoTrack.release();
        videoTrack.release();
    }

    /*
     * Concrete video track to test functionality in abstract class.
     */
    private static class InstrumentationTestVideoTrack extends VideoTrack {
        InstrumentationTestVideoTrack(org.webrtc.VideoTrack webRtcVideoTrack) {
            super(webRtcVideoTrack, true, "");
        }
    }
}
