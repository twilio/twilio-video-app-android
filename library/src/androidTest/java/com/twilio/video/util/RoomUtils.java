/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.util;

import android.support.annotation.Nullable;

import com.twilio.video.VideoCodec;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.twilioapi.VideoApiUtils;
import com.twilio.video.twilioapi.model.VideoRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomUtils {

    public static VideoRoom createRoom(String name, Topology topology) {
        return createRoom(name, topology, false, null);
    }

    public static VideoRoom createRoom(String name,
                                       Topology topology,
                                       boolean enableRecording) {
        return createRoom(name, topology, enableRecording, null);
    }

    public static VideoRoom createRoom(String name,
                                       Topology topology,
                                       boolean enableRecording,
                                       @Nullable List<VideoCodec> videoCodecs) {
        Preconditions.checkNotNull(BuildConfig.twilioCredentials,
            CredentialsUtils.TWILIO_VIDEO_JSON_NOT_PROVIDED);
        Map<String, String> credentials = CredentialsUtils.resolveCredentials(
            Environment.fromString(BuildConfig.ENVIRONMENT));
        String type = VideoApiUtils.P2P;
        boolean enableTurn = false;
        List<String> videoCodecStrings = (videoCodecs == null || videoCodecs.isEmpty()) ?
                (null) :
                codecListToStringList(videoCodecs);
        if (topology == Topology.P2P) {
            enableTurn = true;
        } else {
            type = VideoApiUtils.GROUP;
        }
        return VideoApiUtils.createRoom(credentials.get(CredentialsUtils.ACCOUNT_SID),
                credentials.get(CredentialsUtils.API_KEY),
                credentials.get(CredentialsUtils.API_KEY_SECRET),
                name,
                type,
                BuildConfig.ENVIRONMENT,
                enableTurn,
                enableRecording,
                videoCodecStrings);
    }

    private static <T extends Enum> List<String> codecListToStringList(List<T> enumList) {
        List<String> enumStrings = new ArrayList<>();

        for (T e : enumList) {
            enumStrings.add(e.name());
        }

        return enumStrings;
    }
}
