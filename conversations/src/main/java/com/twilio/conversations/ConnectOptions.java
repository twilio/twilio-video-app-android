package com.twilio.conversations;

public class ConnectOptions {

    private String roomName;
    private String roomSid;
    private boolean createRoom;
    private LocalMedia localMedia;
    private IceOptions iceOptions;

    private ConnectOptions(Builder builder) {
        this.roomName = builder.roomName;
        this.roomSid = builder.roomSid;
        this.createRoom = builder.createRoom;
        this.localMedia = builder.localMedia;
        this.iceOptions = builder.iceOptions;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomSid() {
        return roomSid;
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
        private String roomName;
        private String roomSid;
        private boolean createRoom;
        private LocalMedia localMedia;
        private IceOptions iceOptions;

        public Builder() { }

        public Builder roomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Builder roomSid(String roomSid) {
            this.roomSid = roomSid;
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