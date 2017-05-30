package com.twilio.video.twilioapi.model;

import com.google.gson.annotations.SerializedName;

public class VideoRoom {
    private String status;

    @SerializedName("unique_name")
    private String uniqueName;

    @SerializedName("date_updated")
    private String dateUpdated;

    @SerializedName("max_participants")
    private int maxParticipants;

    @SerializedName("record_participants_on_connect")
    private boolean recordParticipantOnConnect;

    @SerializedName("enable_turn")
    private boolean enableTurn;

    @SerializedName("account_sid")
    private String accountSid;

    private String url;

    @SerializedName("end_time")
    private String endTime;

    private String sid;

    private String duration;

    @SerializedName("date_created")
    private String dateCreated;

    private String type;

    @SerializedName("status_callback_method")
    private String statusCallbackMethod;

    @SerializedName("status_callback")
    private String statusCallback;

    private Links links;

    public VideoRoom() {}

    public String getStatus() {
        return status;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public boolean isRecordParticipantOnConnect() {
        return recordParticipantOnConnect;
    }

    public boolean isEnableTurn() {
        return enableTurn;
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getUrl() {
        return url;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSid() {
        return sid;
    }

    public String getDuration() {
        return duration;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getType() {
        return type;
    }

    public String getStatusCallbackMethod() {
        return statusCallbackMethod;
    }

    public String getStatusCallback() {
        return statusCallback;
    }

    public Links getLinks() {
        return links;
    }
}
