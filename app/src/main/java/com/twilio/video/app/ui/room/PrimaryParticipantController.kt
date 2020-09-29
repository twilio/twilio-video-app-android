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
package com.twilio.video.app.ui.room

import com.twilio.video.VideoTrack
import com.twilio.video.app.sdk.VideoTrackViewState

internal class PrimaryParticipantController(
    /** Primary video track.  */
    private val primaryView: ParticipantPrimaryView
) {
    /**
     * Data container about primary participant - sid, identity, video track, audio state and
     * mirroring state.
     */
    private var primaryItem: Item? = null

    fun renderAsPrimary(
        sid: String?,
        identity: String?,
        screenTrack: VideoTrackViewState?,
        videoTrack: VideoTrackViewState?,
        muted: Boolean,
        mirror: Boolean
    ) {

        val old = primaryItem
        val selectedTrack = screenTrack?.videoTrack ?: videoTrack?.videoTrack
        val newItem = Item(sid, identity, selectedTrack, muted, mirror)

        // clean old primary video renderings
        old?.let { removeRender(it.videoTrack, primaryView) }

        primaryItem = newItem
        primaryView.setIdentity(newItem.identity)
        primaryView.showIdentityBadge(true)
        primaryView.setMuted(newItem.muted)
        primaryView.setMirror(mirror)
        newItem.videoTrack?.let { newVideoTrack ->
            newVideoTrack.addSink(primaryView)
            primaryView.setState(ParticipantView.State.VIDEO)
        } ?: primaryView.setState(ParticipantView.State.NO_VIDEO)
    }

    private fun removeRender(videoTrack: VideoTrack?, view: ParticipantView) {
        if (videoTrack == null || !videoTrack.sinks.contains(view)) return
        videoTrack.removeSink(view)
    }

    /** RemoteParticipant information data holder.  */
    internal class Item(
        /** RemoteParticipant unique identifier.  */
        var sid: String?,
        /** RemoteParticipant name.  */
        var identity: String?,
        /** RemoteParticipant video track.  */
        var videoTrack: VideoTrack?,
        /** RemoteParticipant audio state.  */
        var muted: Boolean,
        /** Video track mirroring enabled/disabled.  */
        var mirror: Boolean
    )
}
