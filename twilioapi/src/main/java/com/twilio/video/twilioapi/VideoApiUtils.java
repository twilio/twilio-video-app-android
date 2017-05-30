package com.twilio.video.twilioapi;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.twilio.video.twilioapi.model.TwilioServiceToken;
import com.twilio.video.twilioapi.model.VideoRoom;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public class VideoApiUtils {
    private static final String PROD_BASE_URL = "https://video.twilio.com";
    private static final String STAGE_BASE_URL = "https://video.stage.twilio.com";
    private static final String DEV_BASE_URL = "https://video.dev.twilio.com";

    public static final String PROD = "prod";
    public static final String STAGE = "stage";
    public static final String DEV = "dev";

    public static final String P2P = "peer-to-peer";
    public static final String GROUP = "group";

    private static String currentEnvironment = PROD;

    interface VideoApiService {
        @POST("/v1/Rooms")
        @FormUrlEncoded
        void createRoom(@Header("Authorization") String authorization,
                        @Field("UniqueName") String name,
                        @Field("Type") String type,
                        @Field("EnableTurn") boolean enableTurn,
                        @Field("RecordParticipantsOnConnect") boolean enableRecording,
                        Callback<VideoRoom> videoRoomCallback);

        @POST("/v1/Rooms")
        @FormUrlEncoded
        VideoRoom createRoom(@Header("Authorization") String authorization,
                             @Field("UniqueName") String name,
                             @Field("Type") String type,
                             @Field("EnableTurn") boolean enableTurn,
                             @Field("RecordParticipantsOnConnect") boolean enableRecording);
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
            .build()
            .create(VideoApiUtils.VideoApiService.class);
    }

    public static void createRoom(String accountSid,
                                  String signingKeySid,
                                  String signingKeySecret,
                                  String name,
                                  String type,
                                  String environment,
                                  boolean enableTurn,
                                  boolean enableRecording,
                                  Callback<VideoRoom> callback)
        throws IllegalArgumentException {
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!type.equalsIgnoreCase(P2P) &&
            !type.equalsIgnoreCase(GROUP)) {
            throw new IllegalArgumentException("Invalid Room Type!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            videoApiService = createVideoApiService();
        }
        String authString = signingKeySid + ":" + signingKeySecret;
        String authorization = "Basic " + Base64.encodeToString(authString.getBytes(),
            Base64.NO_WRAP);
        videoApiService.createRoom(authorization, name, type,
            enableTurn, enableRecording, callback);
    }

    // Provide a synchronous version of createRoom for tests
    public static VideoRoom createRoom(String accountSid,
                                       String signingKeySid,
                                       String signingKeySecret,
                                       String name,
                                       String type,
                                       String environment,
                                       boolean enableTurn,
                                       boolean enableRecording) {
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!type.equalsIgnoreCase(P2P) &&
            !type.equalsIgnoreCase(GROUP)) {
            throw new IllegalArgumentException("Invalid Room Type!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            videoApiService = createVideoApiService();
        }

        String authString = signingKeySid + ":" + signingKeySecret;
        String authorization = "Basic " + Base64.encodeToString(authString.getBytes(),
            Base64.NO_WRAP);

        return videoApiService.createRoom(authorization, name, type, enableTurn, enableRecording);
    }
}
