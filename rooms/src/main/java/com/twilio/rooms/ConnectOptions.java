package com.twilio.rooms;

public class ConnectOptions {

    private String name;
    private boolean createRoom;
    private LocalMedia localMedia;
    private IceOptions iceOptions;

    private ConnectOptions(Builder builder) {
        this.name = builder.name;
        this.createRoom = builder.createRoom;
        this.localMedia = builder.localMedia;
        this.iceOptions = builder.iceOptions;
    }

    public String getName() {
        return name;
    }

    public boolean willCreateRoom() {
        return createRoom;
    }

    public LocalMedia getLocalMedia() {
        return localMedia;
    }

    public IceOptions getIceOptions() {
        return iceOptions;
    }

    public static class Builder {
        private String name;
        private boolean createRoom;
        private LocalMedia localMedia;
        private IceOptions iceOptions;

        public Builder() { }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder createRoom(boolean createRoom) {
            this.createRoom = createRoom;
            return this;
        }

        public Builder localMedia(LocalMedia localMedia) {
            this.localMedia = localMedia;
            return this;
        }

        public Builder iceOptions(IceOptions iceOptions) {
            this.iceOptions = iceOptions;
            return this;
        }

        public ConnectOptions build() {
            return new ConnectOptions(this);
        }
    }

}