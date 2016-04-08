package com.twilio.conversations;


public class ClientOptions {

    private IceOptions iceOptions;

    public ClientOptions() {}

    public ClientOptions(IceOptions iceOptions) {
        this.iceOptions = iceOptions;
    }

    public IceOptions getIceOptions() {
        return iceOptions;
    }

    public void setIceOptions(IceOptions iceOptions) {
        this.iceOptions = iceOptions;
    }

}
