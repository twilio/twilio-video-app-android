package com.twilio.video.internal;

import java.util.Map;

public class ClientOptionsInternal {

    private Map<String, String> privateOptions;

    public ClientOptionsInternal(Map<String, String> privateOptions) {
        this.privateOptions = privateOptions;
    }

    public Map<String, String> getPrivateOptions() {
        return privateOptions;
    }
}
