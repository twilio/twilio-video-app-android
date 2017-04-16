package com.twilio.video.app.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ConfigurationProfileSids {
    @SerializedName("P2P")
    public final String p2p;

    @SerializedName("SFU")
    public final String sfu;

    @SerializedName("SFU Recording")
    public final String sfuRecording;

    public ConfigurationProfileSids(String p2p, String sfu, String sfuRecording) {
        this.p2p = p2p;
        this.sfu = sfu;
        this.sfuRecording = sfuRecording;
    }
}
