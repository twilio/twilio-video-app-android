/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
