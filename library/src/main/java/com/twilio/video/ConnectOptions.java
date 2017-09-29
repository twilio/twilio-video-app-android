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

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents options when connecting to a {@link Room}.
 */
public class ConnectOptions {
    private final String accessToken;
    private final String roomName;
    private final List<LocalAudioTrack> audioTracks;
    private final List<LocalVideoTrack> videoTracks;
    private final List<LocalDataTrack> dataTracks;
    private final IceOptions iceOptions;
    private final boolean enableInsights;
    private final List<AudioCodec> preferredAudioCodecs;
    private final List<VideoCodec> preferredVideoCodecs;
    private final EncodingParameters encodingParameters;

    static void checkAudioTracksReleased(@Nullable List<LocalAudioTrack> audioTracks) {
        if (audioTracks != null) {
            for (LocalAudioTrack localAudioTrack : audioTracks) {
                Preconditions.checkState(
                        !localAudioTrack.isReleased(),
                        "LocalAudioTrack cannot be released");
            }
        }
    }

    static void checkVideoTracksReleased(@Nullable List<LocalVideoTrack> videoTracks) {
        if (videoTracks != null) {
            for (LocalVideoTrack localVideoTrack : videoTracks) {
                Preconditions.checkState(
                        !localVideoTrack.isReleased(),
                        "LocalVideoTrack cannot be released");
            }
        }
    }

    private ConnectOptions(Builder builder) {
        this.accessToken = builder.accessToken;
        this.roomName = builder.roomName;
        this.audioTracks = builder.audioTracks;
        this.videoTracks = builder.videoTracks;
        this.dataTracks = builder.dataTracks;
        this.iceOptions = builder.iceOptions;
        this.enableInsights = builder.enableInsights;
        this.preferredAudioCodecs = builder.preferredAudioCodecs;
        this.preferredVideoCodecs = builder.preferredVideoCodecs;
        this.encodingParameters = builder.encodingParameters;
    }

    String getAccessToken() {
        return accessToken;
    }

    String getRoomName() {
        return roomName;
    }

    List<LocalAudioTrack> getAudioTracks() {
        return audioTracks;
    }

    List<LocalVideoTrack> getVideoTracks() {
        return videoTracks;
    }

    List<LocalDataTrack> getDataTracks() {
        return dataTracks;
    }

    IceOptions getIceOptions() {
        return iceOptions;
    }

    boolean isInsightsEnabled() {
        return enableInsights;
    }

    private LocalAudioTrack[] getLocalAudioTracksArray() {
        LocalAudioTrack[] audioTracksArray = new LocalAudioTrack[0];
        if (audioTracks != null && audioTracks.size() > 0) {
            audioTracksArray = new LocalAudioTrack[audioTracks.size()];
            audioTracksArray = audioTracks.toArray(audioTracksArray);
        }
        return audioTracksArray;
    }

    private LocalVideoTrack[] getLocalVideoTracksArray() {
        LocalVideoTrack[] videoTracksArray = new LocalVideoTrack[0];
        if (videoTracks != null && videoTracks.size() > 0) {
            videoTracksArray = new LocalVideoTrack[videoTracks.size()];
            videoTracksArray = videoTracks.toArray(videoTracksArray);
        }
        return videoTracksArray;
    }

    private LocalDataTrack[] getLocalDataTracksArray() {
        LocalDataTrack[] dataTracksArray = new LocalDataTrack[0];
        if (dataTracks != null && dataTracks.size() > 0) {
            dataTracksArray = new LocalDataTrack[dataTracks.size()];
            dataTracksArray = dataTracks.toArray(dataTracksArray);
        }
        return dataTracksArray;
    }

    private AudioCodec[] getAudioCodecsArray() {
        AudioCodec[] audioCodecsArray = new AudioCodec[0];
        if (preferredAudioCodecs != null && !preferredAudioCodecs.isEmpty()) {
            audioCodecsArray = new AudioCodec[preferredAudioCodecs.size()];
            audioCodecsArray = preferredAudioCodecs.toArray(audioCodecsArray);
        }
        return audioCodecsArray;
    }

    private VideoCodec[] getVideoCodecsArray() {
        VideoCodec[] videoCodecsArray = new VideoCodec[0];
        if (preferredVideoCodecs != null && !preferredVideoCodecs.isEmpty()) {
            videoCodecsArray = new VideoCodec[preferredVideoCodecs.size()];
            videoCodecsArray = preferredVideoCodecs.toArray(videoCodecsArray);
        }
        return videoCodecsArray;
    }

    public EncodingParameters getEncodingParameters() {
        return encodingParameters;
    }

    /*
     * Invoked by JNI RoomDelegate to get pointer to twilio::video::ConnectOptions::Builder
     */
    @SuppressWarnings("unused")
    private long createNativeConnectOptionsBuilder() {
        checkAudioTracksReleased(audioTracks);
        checkVideoTracksReleased(videoTracks);

        return nativeCreate(accessToken,
                roomName,
                getLocalAudioTracksArray(),
                getLocalVideoTracksArray(),
                getLocalDataTracksArray(),
                iceOptions,
                enableInsights,
                PlatformInfo.getNativeHandle(),
                getAudioCodecsArray(),
                getVideoCodecsArray(),
                encodingParameters);
    }

    private native long nativeCreate(String accessToken,
                                     String roomName,
                                     LocalAudioTrack[] audioTracks,
                                     LocalVideoTrack[] videoTracks,
                                     LocalDataTrack[] dataTracks,
                                     IceOptions iceOptions,
                                     boolean enableInsights,
                                     long platformInfoNativeHandle,
                                     AudioCodec[] preferredAudioCodecs,
                                     VideoCodec[] preferredVideoCodecs,
                                     EncodingParameters encodingParameters);
    /**
     * Build new {@link ConnectOptions}.
     *
     * <p>All methods are optional.</p>
     */
    public static class Builder {
        private String accessToken = "";
        private String roomName = "";
        private IceOptions iceOptions;
        private List<LocalAudioTrack> audioTracks;
        private List<LocalVideoTrack> videoTracks;
        private List<LocalDataTrack> dataTracks;
        private boolean enableInsights = true;
        private List<AudioCodec> preferredAudioCodecs;
        private List<VideoCodec> preferredVideoCodecs;
        private EncodingParameters encodingParameters;

        public Builder(String accessToken) {
            this.accessToken = accessToken;
        }

        /**
         * The name of the room.
         */
        public Builder roomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        /**
         * Audio tracks that will be published upon connection.
         */
        public Builder audioTracks(List<LocalAudioTrack> audioTracks) {
            Preconditions.checkNotNull(audioTracks, "LocalAudioTrack List must not be null");
            this.audioTracks = new ArrayList<>(audioTracks);
            return this;
        }

        /**
         * Video tracks that will be published upon connection.
         */
        public Builder videoTracks(List<LocalVideoTrack> videoTracks) {
            Preconditions.checkNotNull(videoTracks, "LocalVideoTrack List must not be null");
            this.videoTracks = new ArrayList<>(videoTracks);
            return this;
        }

        /**
         * Data tracks that will be published upon connection.
         */
        public Builder dataTracks(List<LocalDataTrack> dataTracks) {
            this.dataTracks = dataTracks;
            return this;
        }

        /**
         * Custom ICE configuration used to connect to a Room.
         */
        public Builder iceOptions(IceOptions iceOptions) {
            this.iceOptions = iceOptions;
            return this;
        }

        /**
         * Enable sending stats data to Insights. Sending stats data to Insights is enabled
         * by default.
         */
        public Builder enableInsights(boolean enable) {
            this.enableInsights = enable;
            return this;
        }

        /**
         * Set preferred audio codecs. The list specifies which audio codecs would be
         * preferred when negotiating audio between participants. The preferences are applied in
         * the order found in the list starting with the most preferred audio codec to the
         * least preferred audio codec. Audio codec preferences are not guaranteed to be satisfied
         * because not all participants are guaranteed to support all audio codecs.
         * {@link AudioCodec#OPUS} is the default audio codec if no preferences are set.
         *
         * <p>
         *     The following snippet demonstrates how to prefer a single audio codec.
         * </p>
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferAudioCodecs(Collections.singletonList(AudioCodec.ISAC))
         *          .build();
         * </code></pre>
         *
         * <p>
         *     The following snippet demonstrates how to specify the exact order of codec
         *     preferences.
         * </p>
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferAudioCodecs(Arrays.asList(AudioCodec.ISAC,
         *                  AudioCodec.G722, AudioCodec.OPUS))
         *          .build();
         * </code></pre>
         */
        public Builder preferAudioCodecs(List<AudioCodec> preferredAudioCodecs) {
            this.preferredAudioCodecs = new ArrayList<>(preferredAudioCodecs);
            return this;
        }

        /**
         * Set preferred video codecs. The list specifies which video codecs would be
         * preferred when negotiating video between participants. The preferences are applied in
         * the order found in the list starting with the most preferred video codec to the
         * least preferred video codec. Video codec preferences are not guaranteed to be satisfied
         * because not all participants are guaranteed to support all video codecs.
         * {@link VideoCodec#VP8} is the default video codec if no preferences are set.
         *
         * <p>
         *     The following snippet demonstrates how to prefer a single video codec.
         * </p>
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferVideoCodecs(Collections.singletonList(VideoCodec.H264))
         *          .build();
         * </code></pre>
         *
         * <p>
         *     The following snippet demonstrates how to specify the exact order of codec
         *     preferences.
         * </p>
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferVideoCodecs(Arrays.asList(VideoCodec.H264,
         *                  VideoCodec.VP8, VideoCodec.VP9))
         *          .build();
         * </code></pre>
         */
        public Builder preferVideoCodecs(List<VideoCodec> preferredVideoCodecs) {
            this.preferredVideoCodecs = new ArrayList<>(preferredVideoCodecs);
            return this;
        }

        /**
         * Set {@link EncodingParameters} for audio and video tracks shared to a {@link Room}.
         */
        public Builder encodingParameters(@Nullable EncodingParameters encodingParameters) {
            this.encodingParameters = encodingParameters;
            return this;
        }

        /**
         * Builds {@link ConnectOptions} object.
         * @throws Exception if accessToken is null or empty.
         */
        public ConnectOptions build() {
            Preconditions.checkNotNull(accessToken, "Token must not be null.");
            Preconditions.checkArgument(!accessToken.equals(""), "Token must not be empty.");

            checkAudioTracksReleased(audioTracks);
            checkVideoTracksReleased(videoTracks);

            return new ConnectOptions(this);
        }
    }
}