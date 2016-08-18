package com.twilio.video;

public class ConnectOptions {

    private String name;
    private LocalMedia localMedia;
    private IceOptions iceOptions;

    private ConnectOptions(Builder builder) {
        this.name = builder.name;
        this.localMedia = builder.localMedia;
        this.iceOptions = builder.iceOptions;
    }

    public String getName() {
        return name;
    }

    public LocalMedia getLocalMedia() {
        return localMedia;
    }

    public IceOptions getIceOptions() {
        return iceOptions;
    }

    private long createNativeObject() {
        return nativeCreate(name, localMedia, iceOptions);
    }

    private native long nativeCreate(String name,
                                     LocalMedia localMedia, IceOptions iceOptions);


    public static class Builder {
        private String name = "";
        private LocalMedia localMedia;
        private IceOptions iceOptions;

        public Builder() { }

        public Builder name(String name) {
            this.name = name;
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