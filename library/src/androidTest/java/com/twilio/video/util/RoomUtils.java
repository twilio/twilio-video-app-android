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
import android.util.Log;
import com.twilio.video.Room;
import com.twilio.video.VideoCodec;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.twilioapi.VideoApiUtils;
import com.twilio.video.twilioapi.model.VideoRoom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RoomUtils {
    private static final String TAG = "RoomUtils";

    public static VideoRoom createRoom(String name, Topology topology) {
        return createRoom(name, topology, false, null);
    }

    public static VideoRoom createRoom(String name, Topology topology, boolean enableRecording) {
        return createRoom(name, topology, enableRecording, null);
    }

    public static VideoRoom createRoom(
            String name,
            Topology topology,
            boolean enableRecording,
            @Nullable List<VideoCodec> videoCodecs) {
        Preconditions.checkNotNull(
                BuildConfig.twilioCredentials, CredentialsUtils.TWILIO_VIDEO_JSON_NOT_PROVIDED);
        Map<String, String> credentials =
                CredentialsUtils.resolveCredentials(
                        Environment.fromString(BuildConfig.ENVIRONMENT));
        String type = VideoApiUtils.P2P;
        boolean enableTurn = false;
        List<String> videoCodecStrings =
                (videoCodecs == null || videoCodecs.isEmpty())
                        ? (null)
                        : codecListToStringList(videoCodecs);
        if (topology == Topology.P2P) {
            enableTurn = true;
        } else if (topology == Topology.GROUP) {
            type = VideoApiUtils.GROUP;
        } else if (topology == Topology.GROUP_SMALL) {
            type = VideoApiUtils.GROUP_SMALL;
        }
        return VideoApiUtils.createRoom(
                credentials.get(CredentialsUtils.ACCOUNT_SID),
                credentials.get(CredentialsUtils.API_KEY),
                credentials.get(CredentialsUtils.API_KEY_SECRET),
                name,
                type,
                BuildConfig.ENVIRONMENT,
                enableTurn,
                enableRecording,
                videoCodecStrings);
    }

    private static List<String> codecListToStringList(List<VideoCodec> videoCodecs) {
        List<String> videoCodecStrings = new ArrayList<>();

        for (VideoCodec videoCodec : videoCodecs) {
            videoCodecStrings.add(videoCodec.getName());
        }

        return videoCodecStrings;
    }

    public static @Nullable VideoRoom completeRoom(Room room) {
        Preconditions.checkNotNull(
                BuildConfig.twilioCredentials, CredentialsUtils.TWILIO_VIDEO_JSON_NOT_PROVIDED);
        Map<String, String> credentials =
                CredentialsUtils.resolveCredentials(
                        Environment.fromString(BuildConfig.ENVIRONMENT));

        VideoRoom videoRoom = null;
        try {
            videoRoom =
                    VideoApiUtils.completeRoom(
                            credentials.get(CredentialsUtils.API_KEY),
                            credentials.get(CredentialsUtils.API_KEY_SECRET),
                            room.getSid(),
                            BuildConfig.ENVIRONMENT);
        } catch (RetrofitError e) {
            Response errorResponse = e.getResponse();

            if (errorResponse != null) {
                Log.w(TAG, "Failed to complete room: " + errorResponse.getReason());
            }
        }

        return videoRoom;
    }
}
