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

/**
 * Defines audio and video encoding parameters. Maximum bitrate is specified as Transport
 * Independent Application Specific Maximum (TIAS) bitrate <a
 * href="https://tools.ietf.org/html/rfc3890">RFC3890</a> in bits per second (bps) excluding
 * IP/UDP/TCP headers. These encoding parameters are applied for each PeerConnection. For
 * peer-to-peer Rooms, there is a separate PeerConnection for each participant, i.e., if you set
 * maximum video bitrate to 1 Mbps and you have two {@link RemoteParticipant}`s in the Room, the
 * client sends up to 2 Mbps. For group Rooms, there is a single PeerConnection to Twilio's Media
 * Server. If you are publishing multiple video tracks (e.g., video and screen share), each tracks
 * receives the maximum bitrate specified, i.e., if you set maximum video bitrate to 1 Mbps and you
 * publish both video and screen share, client sends out 2 Mbps. You may update encoding parameters
 * any time using {@link LocalParticipant#setEncodingParameters(EncodingParameters)}.
 */
public class EncodingParameters {
    /**
     * Maximum audio send bitrate in bits per second. Zero indicates the WebRTC default value, which
     * is codec dependent. The maximum bitrate for <a href="http://opus-codec.org/">Opus</a> is 510
     * kbps.
     */
    public final int maxAudioBitrate;

    /**
     * Maximum video send bitrate in bits per second. Zero indicates the WebRTC default value, which
     * is 2 Mbps.
     */
    public final int maxVideoBitrate;

    public EncodingParameters(int maxAudioBitrate, int maxVideoBitrate) {
        this.maxAudioBitrate = maxAudioBitrate;
        this.maxVideoBitrate = maxVideoBitrate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodingParameters that = (EncodingParameters) o;

        return maxAudioBitrate == that.maxAudioBitrate && maxVideoBitrate == that.maxVideoBitrate;
    }

    @Override
    public int hashCode() {
        int result = maxAudioBitrate;
        result = 31 * result + maxVideoBitrate;
        return result;
    }

    @Override
    public String toString() {
        return "EncodingParameters{"
                + "maxAudioBitrate="
                + maxAudioBitrate
                + ", maxVideoBitrate="
                + maxVideoBitrate
                + '}';
    }
}
