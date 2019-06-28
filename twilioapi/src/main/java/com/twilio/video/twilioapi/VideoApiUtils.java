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

package com.twilio.video.twilioapi;

import android.util.Base64;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.twilio.video.twilioapi.model.VideoRoom;
import java.util.List;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public class VideoApiUtils {
    private static final int MAX_RETRIES = 3;
    private static final String PROD_BASE_URL = "https://video.twilio.com";
    private static final String STAGE_BASE_URL = "https://video.stage.twilio.com";
    private static final String DEV_BASE_URL = "https://video.dev.twilio.com";

    public static final String PROD = "prod";
    public static final String STAGE = "stage";
    public static final String DEV = "dev";

    public static final String P2P = "peer-to-peer";
    public static final String GROUP = "group";
    public static final String GROUP_SMALL = "group-small";

    private static String currentEnvironment = PROD;

    interface VideoApiService {
        @POST("/v1/Rooms")
        @FormUrlEncoded
        void createRoom(
                @Header("Authorization") String authorization,
                @Field("UniqueName") String name,
                @Field("Type") String type,
                @Field("EnableTurn") boolean enableTurn,
                @Field("RecordParticipantsOnConnect") boolean enableRecording,
                @Field("VideoCodecs") List<String> videoCodecs,
                @Field("region") String region,
                @Field("media_region") String mediaRegion,
                Callback<VideoRoom> videoRoomCallback);

        @POST("/v1/Rooms")
        @FormUrlEncoded
        VideoRoom createRoom(
                @Header("Authorization") String authorization,
                @Field("UniqueName") String name,
                @Field("Type") String type,
                @Field("EnableTurn") boolean enableTurn,
                @Field("RecordParticipantsOnConnect") boolean enableRecording,
                @Field("VideoCodecs") List<String> videoCodecs,
                @Field("region") String region,
                @Field("media_region") String mediaRegion);

        @GET("/v1/Rooms/{unique_name}")
        VideoRoom getRoom(
                @Header("Authorization") String authorization, @Path("unique_name") String name);

        @POST("/v1/Rooms/{room_sid}")
        @FormUrlEncoded
        VideoRoom modifyRoom(
                @Header("Authorization") String authorization,
                @Path("room_sid") String roomSid,
                @Field("Status") String status);
    }

    private static VideoApiUtils.VideoApiService videoApiService = createVideoApiService();

    private static VideoApiUtils.VideoApiService createVideoApiService() {
        String apiBaseUrl = PROD_BASE_URL;
        if (currentEnvironment.equalsIgnoreCase(STAGE)) {
            apiBaseUrl = STAGE_BASE_URL;
        } else if (currentEnvironment.equalsIgnoreCase(DEV)) {
            apiBaseUrl = DEV_BASE_URL;
        }
        return new RestAdapter.Builder()
                .setEndpoint(apiBaseUrl)
                .setConverter(new GsonConverter(new GsonBuilder().create()))
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .build()
                .create(VideoApiUtils.VideoApiService.class);
    }

    public static void createRoom(
            String accountSid,
            String signingKeySid,
            String signingKeySecret,
            String name,
            String type,
            String environment,
            String region,
            String mediaRegion,
            boolean enableTurn,
            boolean enableRecording,
            List<String> videoCodecs,
            Callback<VideoRoom> callback)
            throws IllegalArgumentException {
        if (!environment.equalsIgnoreCase(PROD)
                && !environment.equalsIgnoreCase(STAGE)
                && !environment.equalsIgnoreCase(DEV)) {
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!type.equalsIgnoreCase(P2P)
                && !type.equalsIgnoreCase(GROUP)
                && !type.equalsIgnoreCase(GROUP_SMALL)) {
            throw new IllegalArgumentException("Invalid Room Type!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            videoApiService = createVideoApiService();
        }
        if (region == null) {
            region = "gll";
        }
        if (mediaRegion == null) {
            mediaRegion = "gll";
        }
        String authString = signingKeySid + ":" + signingKeySecret;
        String authorization =
                "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        videoApiService.createRoom(
                authorization,
                name,
                type,
                enableTurn,
                enableRecording,
                videoCodecs,
                region,
                mediaRegion,
                callback);
    }

    // Provide a synchronous version of createRoom for tests
    public static VideoRoom createRoom(
            String accountSid,
            String signingKeySid,
            String signingKeySecret,
            String name,
            String type,
            String environment,
            String region,
            String mediaRegion,
            boolean enableTurn,
            boolean enableRecording,
            List<String> videoCodecs) {
        if (!environment.equalsIgnoreCase(PROD)
                && !environment.equalsIgnoreCase(STAGE)
                && !environment.equalsIgnoreCase(DEV)) {
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!type.equalsIgnoreCase(P2P)
                && !type.equalsIgnoreCase(GROUP)
                && !type.equalsIgnoreCase(GROUP_SMALL)) {
            throw new IllegalArgumentException("Invalid Room Type!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            videoApiService = createVideoApiService();
        }

        if (region == null) {
            region = "gll";
        }
        if (mediaRegion == null) {
            mediaRegion = "gll";
        }
        String authString = signingKeySid + ":" + signingKeySecret;
        String authorization =
                "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        /*
         * Occasionally requests to create a room time out on Firebase Test Lab. Retry a reasonable
         * amount of times before failing.
         */
        int retries = 0;
        VideoRoom videoRoom = null;
        do {
            try {
                videoRoom =
                        videoApiService.createRoom(
                                authorization,
                                name,
                                type,
                                enableTurn,
                                enableRecording,
                                videoCodecs,
                                region,
                                mediaRegion);
            } catch (RetrofitError createRoomError) {
                Log.e(
                        "VideoApiUtils",
                        String.format("RetrofitError: %s", createRoomError.getKind().name()));
                if (createRoomError.getResponse() != null) {
                    throw createRoomError;
                }
            }

            // Wait some time before trying again
            if (videoRoom == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("VideoApiUtils", e.getMessage());
                }
            }
        } while (videoRoom == null && retries++ < MAX_RETRIES);

        // Validate the room creation succeeded
        if (videoRoom == null) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to create a Room after %s attempts",
                            String.valueOf(MAX_RETRIES)));
        }
        /*
         * Validate the room resource.
         *
         * Note that for a group room the infrastructure will always return enableTurn=true even if
         * the request was made with enableTurn=false.
         */
        boolean expectedEnableTurn =
                type.equalsIgnoreCase(GROUP) || type.equalsIgnoreCase(GROUP_SMALL) || enableTurn;

        boolean isRoomResourceMatchingConfig =
                accountSid.equals(videoRoom.getAccountSid())
                        && name.equals(videoRoom.getUniqueName())
                        && type.equals(videoRoom.getType())
                        && expectedEnableTurn == videoRoom.isEnableTurn()
                        && enableRecording == videoRoom.isRecordParticipantOnConnect();
        if (!isRoomResourceMatchingConfig) {
            throw new IllegalStateException(
                    "Room resource does not match configuration requested for test");
        }

        return videoRoom;
    }

    public static VideoRoom completeRoom(
            String signingKeySid, String signingKeySecret, String roomSid, String environment) {
        if (!environment.equalsIgnoreCase(PROD)
                && !environment.equalsIgnoreCase(STAGE)
                && !environment.equalsIgnoreCase(DEV)) {
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            videoApiService = createVideoApiService();
        }

        String authString = signingKeySid + ":" + signingKeySecret;
        String authorization =
                "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

        return videoApiService.modifyRoom(authorization, roomSid, "completed");
    }
}
