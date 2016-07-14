package com.twilio.rooms.internal;

import com.twilio.rooms.IceOptions;

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
