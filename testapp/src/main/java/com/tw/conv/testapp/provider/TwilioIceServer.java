package com.tw.conv.testapp.provider;

import android.support.annotation.Nullable;

public class TwilioIceServer {
    private String url;
    private String username;
    private String credential;

    public TwilioIceServer() {
        url = "";
        username = "";
        credential = "";
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getCredential() {
        return credential;
    }

    public String toString() {
        return url;
    }
}
