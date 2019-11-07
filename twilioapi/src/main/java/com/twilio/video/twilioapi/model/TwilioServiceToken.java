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
