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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Represents options when connecting to a {@link Room}. */
public class ConnectOptions {
    private static final Set<Class> SUPPORTED_CODECS =
            new HashSet<Class>(
                    Arrays.asList(
                            // Audio codecs
                            IsacCodec.class,
                            OpusCodec.class,
                            PcmuCodec.class,
                            PcmaCodec.class,
                            G722Codec.class,

                            // Video codecs
                            Vp8Codec.class,
                            H264Codec.class,
                            Vp9Codec.class));

    private final String accessToken;
    private final String roomName;
    private final String region;
    private final List<LocalAudioTrack> audioTracks;
    private final List<LocalVideoTrack> videoTracks;
    private final List<LocalDataTrack> dataTracks;
    private final IceOptions iceOptions;
    private final boolean enableInsights;
    private final boolean enableAutomaticSubscription;
    private final boolean enableDominantSpeaker;
    private final List<AudioCodec> preferredAudioCodecs;
    private final List<VideoCodec> preferredVideoCodecs;
    private final EncodingParameters encodingParameters;

    private final MediaFactory mediaFactory;

    static void checkAudioCodecs(@Nullable List<AudioCodec> audioCodecs) {
        if (audioCodecs != null) {
            for (AudioCodec audioCodec : audioCodecs) {
                Preconditions.checkNotNull(audioCodec);
                Preconditions.checkArgument(
                        SUPPORTED_CODECS.contains(audioCodec.getClass()),
                        String.format("Unsupported audio codec %s", audioCodec.getName()));
            }
        }
    }

    static void checkVideoCodecs(@Nullable List<VideoCodec> videoCodecs) {
        if (videoCodecs != null) {
            for (VideoCodec videoCodec : videoCodecs) {
                Preconditions.checkNotNull(videoCodec);
                Preconditions.checkArgument(
                        SUPPORTED_CODECS.contains(videoCodec.getClass()),
                        String.format("Unsupported video codec %s", videoCodec.getName()));
            }
        }
    }

    static void checkAudioTracksReleased(@Nullable List<LocalAudioTrack> audioTracks) {
        if (audioTracks != null) {
            for (LocalAudioTrack localAudioTrack : audioTracks) {
                Preconditions.checkState(
                        !localAudioTrack.isReleased(), "LocalAudioTrack cannot be released");
            }
        }
    }

    static void checkVideoTracksReleased(@Nullable List<LocalVideoTrack> videoTracks) {
        if (videoTracks != null) {
            for (LocalVideoTrack localVideoTrack : videoTracks) {
                Preconditions.checkState(
                        !localVideoTrack.isReleased(), "LocalVideoTrack cannot be released");
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
        this.enableAutomaticSubscription = builder.enableAutomaticSubscription;
        this.enableDominantSpeaker = builder.enableDominantSpeaker;
        this.preferredAudioCodecs = builder.preferredAudioCodecs;
        this.preferredVideoCodecs = builder.preferredVideoCodecs;
        this.region = builder.region;
        this.encodingParameters = builder.encodingParameters;
        this.mediaFactory = builder.mediaFactory;
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

    String getRegion() {
        return region;
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

    EncodingParameters getEncodingParameters() {
        return encodingParameters;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Nullable
    MediaFactory getMediaFactory() {
        return this.mediaFactory;
    }

    /*
     * Invoked by JNI RoomDelegate to get pointer to twilio::video::ConnectOptions::Builder
     */
    @SuppressWarnings("unused")
    private long createNativeConnectOptionsBuilder() {
        checkAudioTracksReleased(audioTracks);
        checkVideoTracksReleased(videoTracks);

        return nativeCreate(
                accessToken,
                roomName,
                getLocalAudioTracksArray(),
                getLocalVideoTracksArray(),
                getLocalDataTracksArray(),
                iceOptions,
                enableInsights,
                enableAutomaticSubscription,
                enableDominantSpeaker,
                PlatformInfo.getNativeHandle(),
                getAudioCodecsArray(),
                getVideoCodecsArray(),
                region,
                encodingParameters);
    }

    private native long nativeCreate(
            String accessToken,
            String roomName,
            LocalAudioTrack[] audioTracks,
            LocalVideoTrack[] videoTracks,
            LocalDataTrack[] dataTracks,
            IceOptions iceOptions,
            boolean enableInsights,
            boolean enableAutomaticSubscription,
            boolean enableDominantSpeaker,
            long platformInfoNativeHandle,
            AudioCodec[] preferredAudioCodecs,
            VideoCodec[] preferredVideoCodecs,
            String region,
            EncodingParameters encodingParameters);

    /**
     * Build new {@link ConnectOptions}.
     *
     * <p>All methods are optional.
     */
    public static class Builder {
        private String accessToken = "";
        private String roomName = "";
        private IceOptions iceOptions;
        private List<LocalAudioTrack> audioTracks;
        private List<LocalVideoTrack> videoTracks;
        private List<LocalDataTrack> dataTracks;
        private boolean enableInsights = true;
        private boolean enableAutomaticSubscription = true;
        private boolean enableDominantSpeaker = false;
        private List<AudioCodec> preferredAudioCodecs;
        private List<VideoCodec> preferredVideoCodecs;
        private String region = "gll";
        private EncodingParameters encodingParameters;
        private MediaFactory mediaFactory;

        public Builder(@NonNull String accessToken) {
            Preconditions.checkNotNull(accessToken);
            this.accessToken = accessToken;
        }

        /** The name of the room. */
        @NonNull
        public Builder roomName(@NonNull String roomName) {
            Preconditions.checkNotNull(roomName);
            this.roomName = roomName;
            return this;
        }

        /** Audio tracks that will be published upon connection. */
        @NonNull
        public Builder audioTracks(@NonNull List<LocalAudioTrack> audioTracks) {
            Preconditions.checkNotNull(audioTracks, "LocalAudioTrack List must not be null");
            this.audioTracks = new ArrayList<>(audioTracks);
            return this;
        }

        /** Video tracks that will be published upon connection. */
        @NonNull
        public Builder videoTracks(@NonNull List<LocalVideoTrack> videoTracks) {
            Preconditions.checkNotNull(videoTracks, "LocalVideoTrack List must not be null");
            this.videoTracks = new ArrayList<>(videoTracks);
            return this;
        }

        /** Data tracks that will be published upon connection. */
        @NonNull
        public Builder dataTracks(@NonNull List<LocalDataTrack> dataTracks) {
            Preconditions.checkNotNull(dataTracks);
            this.dataTracks = dataTracks;
            return this;
        }

        /** Custom ICE configuration used to connect to a Room. */
        @NonNull
        public Builder iceOptions(@NonNull IceOptions iceOptions) {
            Preconditions.checkNotNull(iceOptions);
            this.iceOptions = iceOptions;
            return this;
        }

        /**
         * Enable sending stats data to Insights. Sending stats data to Insights is enabled by
         * default.
         */
        @NonNull
        public Builder enableInsights(boolean enable) {
            this.enableInsights = enable;
            return this;
        }

        /**
         * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
         * notifications of track publish events, but will not automatically subscribe to them. If
         * set to true, the LocalParticipant will automatically subscribe to tracks as they are
         * published. If unset, the default is true. Note: This feature is only available for Group
         * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
         */
        @NonNull
        public Builder enableAutomaticSubscription(boolean enableAutomaticSubscription) {
            this.enableAutomaticSubscription = enableAutomaticSubscription;
            return this;
        }

        /**
         * Enable reporting of a {@link Room}'s dominant speaker. This option has no effect if the
         * {@link Room} topology is P2P.
         *
         * @see Room#getDominantSpeaker()
         * @see Room.Listner#onDominantSpeakerChanged()
         */
        @NonNull
        public Builder enableDominantSpeaker(boolean enableDominantSpeaker) {
            this.enableDominantSpeaker = enableDominantSpeaker;
            return this;
        }

        /**
         * Set preferred audio codecs. The list specifies which audio codecs would be preferred when
         * negotiating audio between participants. The preferences are applied in the order found in
         * the list starting with the most preferred audio codec to the least preferred audio codec.
         * Audio codec preferences are not guaranteed to be satisfied because not all participants
         * are guaranteed to support all audio codecs. {@link OpusCodec} is the default audio codec
         * if no preferences are set.
         *
         * <p>The following snippet demonstrates how to prefer a single audio codec.
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferAudioCodecs(Collections.<AudioCodec>singletonList(new IsacCodec()))
         *          .build();
         * </code></pre>
         *
         * <p>The following snippet demonstrates how to specify the exact order of codec
         * preferences.
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferAudioCodecs(Arrays.asList(new IsacCodec(),
         *                  new G722Codec(), new OpusCodec()))
         *          .build();
         * </code></pre>
         */
        @NonNull
        public Builder preferAudioCodecs(@NonNull List<AudioCodec> preferredAudioCodecs) {
            Preconditions.checkNotNull(preferredAudioCodecs);
            this.preferredAudioCodecs = new ArrayList<>(preferredAudioCodecs);
            return this;
        }

        /**
         * Set preferred video codecs. The list specifies which video codecs would be preferred when
         * negotiating video between participants. The preferences are applied in the order found in
         * the list starting with the most preferred video codec to the least preferred video codec.
         * Video codec preferences are not guaranteed to be satisfied because not all participants
         * are guaranteed to support all video codecs. {@link Vp8Codec} is the default video codec
         * if no preferences are set.
         *
         * <p>The following snippet demonstrates how to prefer a single video codec.
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferVideoCodecs(Collections.<VideoCodec>singletonList(new H264Codec()))
         *          .build();
         * </code></pre>
         *
         * <p>The following snippet demonstrates how to specify the exact order of codec
         * preferences.
         *
         * <pre><code>
         *     ConnectOptions connectOptions = new ConnectOptions.Builder(token)
         *          .preferVideoCodecs(Arrays.asList(new H264Codec(),
         *                  new Vp8Codec(), new Vp9Codec()))
         *          .build();
         * </code></pre>
         */
        @NonNull
        public Builder preferVideoCodecs(@NonNull List<VideoCodec> preferredVideoCodecs) {
            Preconditions.checkNotNull(preferredVideoCodecs);
            this.preferredVideoCodecs = new ArrayList<>(preferredVideoCodecs);
            return this;
        }

        /**
         * The region of the signaling Server the Client will use. By default, the Client will
         * connect to the nearest signaling Server determined by <a
         * href="https://www.twilio.com/docs/video/ip-address-whitelisting#signaling-communication">latency
         * based routing</a>. Setting a value other than "gll" bypasses routing and guarantees that
         * signaling traffic will be terminated in the region that you prefer. If you are connecting
         * to a Group Room created with the "gll" Media Region (either <a
         * href="https://www.twilio.com/console/video/configure">Ad-Hoc</a> or via the <a
         * href="https://www.twilio.com/docs/video/api/rooms-resource#room-instance-resource">REST
         * API</a>), then the Room's Media Region will be selected based upon your Client's region.
         * The default value is `gll`.
         */
        @NonNull
        public Builder region(@NonNull String region) {
            this.region = region;
            return this;
        }

        /** Set {@link EncodingParameters} for audio and video tracks shared to a {@link Room}. */
        @NonNull
        public Builder encodingParameters(@NonNull EncodingParameters encodingParameters) {
            Preconditions.checkNotNull(encodingParameters);
            this.encodingParameters = encodingParameters;
            return this;
        }

        /*
         * Private API for connecting to Room with a custom MediaFactory. Used to simulate
         * participant media scenarios on one device.
         */
        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        Builder mediaFactory(@Nullable MediaFactory mediaFactory) {
            this.mediaFactory = mediaFactory;
            return this;
        }

        /**
         * Builds {@link ConnectOptions} object.
         *
         * @throws Exception if accessToken is null or empty.
         */
        @NonNull
        public ConnectOptions build() {
            Preconditions.checkNotNull(accessToken, "Token must not be null.");
            Preconditions.checkArgument(!accessToken.equals(""), "Token must not be empty.");

            checkAudioTracksReleased(audioTracks);
            checkVideoTracksReleased(videoTracks);
            checkAudioCodecs(preferredAudioCodecs);
            checkVideoCodecs(preferredVideoCodecs);

            return new ConnectOptions(this);
        }
    }
}
