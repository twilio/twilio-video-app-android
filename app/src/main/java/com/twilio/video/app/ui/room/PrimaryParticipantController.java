/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.ui.room;

import com.twilio.video.VideoTrack;

class PrimaryParticipantController {

    /**
     * Data container about primary participant - sid, identity, video track, audio state and
     * mirroring state.
     */
    private Item primaryItem;

    /** Primary video track. */
    private ParticipantPrimaryView primaryView;

    PrimaryParticipantController(ParticipantPrimaryView primaryVideoView) {
        this.primaryView = primaryVideoView;
    }

    void renderAsPrimary(
            String sid,
            String identity,
            VideoTrack screenTrack,
            VideoTrack videoTrack,
            boolean muted,
            boolean mirror) {

        Item old = primaryItem;
        VideoTrack selectedTrack = screenTrack != null ? screenTrack : videoTrack;
        Item newItem = new Item(sid, identity, selectedTrack, muted, mirror);

        // clean old primary video renderings
        if (old != null) {
            removeRender(old.videoTrack, primaryView);
        }

        primaryItem = newItem;
        primaryView.setIdentity(primaryItem.identity);
        primaryView.showIdentityBadge(true);
        primaryView.setMuted(primaryItem.muted);
        primaryView.setMirror(mirror);

        if (primaryItem.videoTrack != null) {
            removeRender(primaryItem.videoTrack, primaryView);
            primaryView.setState(ParticipantView.State.VIDEO);
            primaryItem.videoTrack.addRenderer(primaryView);
        } else {
            primaryView.setState(ParticipantView.State.NO_VIDEO);
        }
    }

    private void removeRender(VideoTrack videoTrack, ParticipantView view) {
        if (videoTrack == null || !videoTrack.getRenderers().contains(view)) return;
        videoTrack.removeRenderer(view);
    }

    /** RemoteParticipant information data holder. */
    static class Item {

        /** RemoteParticipant unique identifier. */
        String sid;

        /** RemoteParticipant name. */
        String identity;

        /** RemoteParticipant video track. */
        VideoTrack videoTrack;

        /** RemoteParticipant audio state. */
        boolean muted;

        /** Video track mirroring enabled/disabled. */
        boolean mirror;

        Item(String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {

            this.sid = sid;
            this.identity = identity;
            this.videoTrack = videoTrack;
            this.muted = muted;
            this.mirror = mirror;
        }
    }
}
