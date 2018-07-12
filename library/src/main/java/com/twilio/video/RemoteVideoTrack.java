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

/** A remote video track represents a remote video source. */
public class RemoteVideoTrack extends VideoTrack {
    private final String sid;

    RemoteVideoTrack(
            org.webrtc.VideoTrack webRtcVideoTrack, String sid, String name, boolean enabled) {
        super(webRtcVideoTrack, enabled, name);
        this.sid = sid;
    }

    /**
     * Returns the remote video track's server identifier. This value uniquely identifies the remote
     * video track within the scope of a {@link Room}.
     */
    public String getSid() {
        return sid;
    }
}
