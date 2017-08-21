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
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.twilio.video.util.VideoAssert.assertFramesRendered;
import static com.twilio.video.util.VideoAssert.assertNoFramesRendered;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class VideoTrackTest {
    private static final int RENDER_FRAME_DELAY_MS = 3000;

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

    @Test
    public void invalidateWebRtcTrack_shouldBeIdempotent() {
        videoTrack.invalidateWebRtcTrack();
        videoTrack.invalidateWebRtcTrack();
    }

    /*
     * This test validates the scenario where a developer can add a renderer to a RemoteVideoTrack
     * after it is added but before it is subscribed to.
     */
    @Test
    public void canAddRendererBeforeWebRtcTrackSet() throws InterruptedException {
        VideoTrack videoTrack = new InstrumentationTestVideoTrack(true);

        // Add renderer before webrtc track is set
        videoTrack.addRenderer(frameCountRenderer);

        // Assert that we see no frames
        assertNoFramesRendered(frameCountRenderer, RENDER_FRAME_DELAY_MS);

        // Set webrtc track
        videoTrack.setWebRtcTrack(localVideoTrack.getWebRtcTrack());

        // Validate that we now render frames
        assertFramesRendered(frameCountRenderer, RENDER_FRAME_DELAY_MS);
    }

    /*
     * This test validates the scenario where a developer can render frames when the following
     * sequence occurs:
     *
     * 1. Remote track is subscribed to
     * 2. Rendered added
     * 2. Remove track is unsubscribed from
     * 3. Remote track is subscribed to again
     */
    @Test
    public void canRenderFramesAfterWebRtcTrackIsReset() throws InterruptedException {
        // Add renderer
        videoTrack.addRenderer(frameCountRenderer);

        // Assert that we see frames
        assertFramesRendered(frameCountRenderer, RENDER_FRAME_DELAY_MS);

        // Invalidate WebRTC track
        videoTrack.invalidateWebRtcTrack();

        // Assert that no frames are received
        assertNoFramesRendered(frameCountRenderer, RENDER_FRAME_DELAY_MS);

        // Set webrtc track
        LocalVideoTrack newVideoTrack = LocalVideoTrack.create(context, true,
                new FakeVideoCapturer());
        videoTrack.setWebRtcTrack(newVideoTrack.getWebRtcTrack());

        // Validate that we render frames again
        assertFramesRendered(frameCountRenderer, RENDER_FRAME_DELAY_MS);

        // Release tracks
        videoTrack.release();
        newVideoTrack.release();
    }

    /*
     * Concrete video track to test functionality in abstract class.
     */
    private static class InstrumentationTestVideoTrack extends VideoTrack {
        private static final int TRACK_ID_LENGTH = 10;

        InstrumentationTestVideoTrack(org.webrtc.VideoTrack webRtcVideoTrack) {
            super(webRtcVideoTrack, true);
        }

        InstrumentationTestVideoTrack(boolean enabled) {
            super(enabled);
        }
    }
}
