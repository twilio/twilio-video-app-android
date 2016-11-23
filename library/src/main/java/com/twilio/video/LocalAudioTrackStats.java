package com.twilio.video;

public class LocalAudioTrackStats extends LocalTrackStats {

    public final int audioInputLevel;
    /**
     * Packet jitter measured in milliseconds
     */
    public final int jitterReceived;
    /**
     * Jitter buffer measured in milliseconds
     */
    public final int jitter;

    LocalAudioTrackStats(Builder builder) {
        super(builder.trackId, builder.packetsLost, builder.direction,
                builder.codecName, builder.ssrc, builder.unixTimestamp,
                builder.bytesSent, builder.packetsSent, builder.roundTripTime);
        this.audioInputLevel = builder.audioInputLevel;
        this.jitterReceived = builder.jitterReceived;
        this.jitter = builder.jitter;
    }

    static final class Builder {
        private int audioInputLevel;
        private int jitterReceived;
        private int jitter;
        private long bytesSent;
        private long packetsSent;
        private int roundTripTime;
        private String trackId;
        private int packetsLost;
        private String direction;
        private String codecName;
        private String ssrc;
        private long unixTimestamp;

        Builder() {}

        Builder audioInputLevel(int audioInputLevel) {
            this.audioInputLevel = audioInputLevel;
            return this;
        }

        Builder jitterReceived(int jitterReceived) {
            this.jitterReceived = jitterReceived;
            return this;
        }

        Builder jitter(int jitter) {
            this.jitter = jitter;
            return this;
        }

        Builder bytesSent(long bytesSent) {
            this.bytesSent = bytesSent;
            return this;
        }

        Builder packetsSent(long packetsSent) {
            this.packetsSent = packetsSent;
            return this;
        }

        Builder roundTripTime(int roundTripTime) {
            this.roundTripTime = roundTripTime
            return this;
        }

        Builder trackId(String trackId) {
            this.trackId = trackId;
            return this;
        }

        Builder packetsLost(int packetsLost) {
            this.packetsLost = packetsLost;
            return this;
        }

        Builder codecName(String codecName) {
            this.codecName = codecName;
            return this;
        }

        Builder ssrc(String ssrc) {
            this.ssrc = ssrc;
            return this;
        }

        Builder unixTimestamp(long unixTimestamp) {
            this.unixTimestamp = unixTimestamp;
            return this;
        }

    }
}
