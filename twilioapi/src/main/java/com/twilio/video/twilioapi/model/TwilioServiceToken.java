package com.twilio.video.twilioapi.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TwilioServiceToken {

    private String username;

    private String password;

    @SerializedName("account_sid")
    private String accountSid;

    private String ttl;

    @SerializedName("ice_servers")
    private List<TwilioIceServer> iceServers;

    @SerializedName("date_created")
    private String dateCreated;

    @SerializedName("date_updated")
    private String dateUpdated;

    public TwilioServiceToken() {}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountSid() {
        return accountSid;
    }

    public List<TwilioIceServer> getIceServers() {
        return iceServers;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public String getTtl() {
        return ttl;
    }
}
