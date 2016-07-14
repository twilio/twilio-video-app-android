package com.twilio.rooms.internal;

import com.twilio.rooms.ClientOptions;
import com.twilio.rooms.IceOptions;

import java.util.Map;

public class ClientOptionsInternal extends ClientOptions {

    private Map<String, String> privateOptions;

    public ClientOptionsInternal(Map<String, String> privateOptions) {
        this.privateOptions = privateOptions;
    }

    public ClientOptionsInternal(IceOptions iceOptions, Map<String, String> privateOptions) {
        super(iceOptions);
        this.privateOptions = privateOptions;
    }

    // Private options
    public void setPrivateOptions(Map<String, String> privateOptions) {
        this.privateOptions = privateOptions;
    }

    public Map<String, String> getPrivateOptions() {
        return privateOptions;
    }
}
