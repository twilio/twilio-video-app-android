package com.twilio.rooms;

/**
 * IceServer is a single STUN or TURN server.
 */
public class IceServer {
    public final String username;
    public final String password;
    public final String serverUrl;

    public IceServer(String serverUrl, String username, String password) {
        this.username = username;
        this.password = password;
        this.serverUrl = serverUrl;
    }

    public IceServer(String serverUrl) {
        this(serverUrl, "", "");
    }
}
