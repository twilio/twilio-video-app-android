package com.twilio.conversations.internal;

import com.twilio.conversations.ClientOptions;
import com.twilio.conversations.IceOptions;

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
