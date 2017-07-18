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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.getkeepsafe.relinker.ReLinker;
import com.twilio.video.util.RandUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class VideoTrackTest {
    private final VideoRenderer videoRenderer = new VideoRenderer() {
        @Override
        public void renderFrame(I420Frame frame) {

        }
    };
    private VideoTrack videoTrack;
    private org.webrtc.VideoTrack fakeWebRtcVideoTrack;

    @Before
    public void setup() {
        ReLinker.loadLibrary(InstrumentationRegistry.getContext(), "jingle_peerconnection_so");
        fakeWebRtcVideoTrack = new FakeWebRtcVideoTrack();
        videoTrack = new InstrumentationTestVideoTrack(fakeWebRtcVideoTrack);
    }

    @Test
    public void shouldAllowAddRendererAfterReleased() {
        videoTrack.release();
        videoTrack.addRenderer(videoRenderer);
    }

    @Test
    public void shouldAllowRemoveRendererAfterReleased() {
        videoTrack.release();
        videoTrack.removeRenderer(videoRenderer);
    }

    /*
     * Concrete video track to test functionality in abstract class.
     */
    private static class InstrumentationTestVideoTrack extends VideoTrack {
        InstrumentationTestVideoTrack(org.webrtc.VideoTrack webRtcVideoTrack) {
            super(webRtcVideoTrack, true);
        }
    }

    /*
     * Fake WebRTC video track that allows testing of VideoTrack.
     */
    private static class FakeWebRtcVideoTrack extends org.webrtc.VideoTrack {
        private static final int ID_LENGTH = 10;
        private static final String TRACK_KIND = "fake";

        private final String id;

        FakeWebRtcVideoTrack() {
            super(new Random().nextLong());
            this.id = RandUtils.generateRandomString(ID_LENGTH);
        }

        @Override
        public void addRenderer(org.webrtc.VideoRenderer renderer) {
            // No-op
        }

        @Override
        public void removeRenderer(org.webrtc.VideoRenderer renderer) {
            // No-op
        }

        @Override
        public void dispose() {
            // No-op
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String kind() {
            return TRACK_KIND;
        }

        @Override
        public boolean enabled() {
            return super.enabled();
        }

        @Override
        public boolean setEnabled(boolean enable) {
            return super.setEnabled(enable);
        }

        @Override
        public State state() {
            return super.state();
        }
    }
}
