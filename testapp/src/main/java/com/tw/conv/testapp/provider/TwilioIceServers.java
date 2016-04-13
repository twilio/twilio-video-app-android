package com.tw.conv.testapp.provider;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TwilioIceServers {

    public String username;

    public String password;

    @SerializedName("account_sid")
    public String accountSid;

    @SerializedName("ice_servers")
    public List<TwilioIceServer> iceServers;

    @SerializedName("date_created")
    public String dateCreated;

    @SerializedName("date_updated")
    public String dateUpdated;
}
