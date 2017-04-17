package com.twilio.video;

/**
 * Represents options when connecting to a {@link Room}.
 */
public class ConnectOptions {
    private final String accessToken;
    private final String roomName;
    private final LocalMedia localMedia;
    private final IceOptions iceOptions;
    private final boolean enableInsights;

    private ConnectOptions(Builder builder) {
        this.accessToken = builder.accessToken;
        this.roomName = builder.roomName;
        this.localMedia = builder.localMedia;
        this.iceOptions = builder.iceOptions;
        this.enableInsights = builder.enableInsights;
    }

    String getAccessToken() {
        return accessToken;
    }

    String getRoomName() {
        return roomName;
    }

    LocalMedia getLocalMedia() {
        return localMedia;
    }

    IceOptions getIceOptions() {
        return iceOptions;
    }

    boolean isInsightsEnabled() {
        return enableInsights;
    }

    private long createNativeObject() {
        return nativeCreate(
            accessToken, roomName, localMedia, iceOptions, enableInsights, PlatformInfo.getNativeHandle());
    }

    private native long nativeCreate(String accessToken,
                                     String roomName,
                                     LocalMedia localMedia,
                                     IceOptions iceOptions,
                                     boolean enableInsights,
                                     long platformInfoNativeHandle);
    /**
     * Build new {@link ConnectOptions}.
     *
     * <p>All methods are optional.</p>
     */
    public static class Builder {
        private String accessToken = "";
        private String roomName = "";
        private LocalMedia localMedia;
        private IceOptions iceOptions;
        private boolean enableInsights;

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
         * Media that will be published upon connection.
         */
        public Builder localMedia(LocalMedia localMedia) {
            this.localMedia = localMedia;
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
         * Enable sending stats data to Insights
         */
        public Builder enableInsights(boolean enable) {
            this.enableInsights = enable;
            return this;
        }

        /**
         * Builds {@link ConnectOptions} object.
         * @throws Exception if accessToken is null or empty.
         */
        public ConnectOptions build() {
            if (accessToken == null) {
                throw new NullPointerException("Token must not be null.");
            }
            if (accessToken.equals("")) {
                throw new IllegalArgumentException("Token must not be empty.");
            }
            return new ConnectOptions(this);
        }
    }
}