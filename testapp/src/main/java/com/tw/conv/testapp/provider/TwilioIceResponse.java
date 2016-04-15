package com.tw.conv.testapp.provider;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TwilioIceResponse {

    public static final String ICE_TRANSPORT_POLICY = "ice_transport_type";
    public static final String ICE_SELECTED_SERVERS = "ice_selected_servers";
    public static final String ICE_SERVERS = "ice_servers";

    private String username;

    private String password;

    @SerializedName("account_sid")
    private String accountSid;

    @SerializedName("ice_servers")
    private List<TwilioIceServer> iceServers;

    @SerializedName("date_created")
    private String dateCreated;

    @SerializedName("date_updated")
    private String dateUpdated;

    public TwilioIceResponse() {}

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
}
