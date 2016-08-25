package com.tw.video.testapp.util;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.tw.video.testapp.model.TwilioIceResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class SimpleSignalingUtils {
    public static final String CAPABILITY_TOKEN = "capability_token";
    public static final String USERNAME = "username";
    public static final String REALM = "realm";
    public static final String ROOM_NAME = "room_name";

    /*
     * The default is usually 30 minutes. We are intentionally setting it to 5 minutes to validate
     * expiration.
     */
    public static final String TTL = "300";
    
    /* Define the Retrofit Token Service */
    interface SimpleSignalingApi {
        @GET("/access-token")
        void getAccessToken(@QueryMap Map<String, String> options,
                            Callback<String> tokenCallback);

        @GET("/ice")
        void getIceServers(@QueryMap Map<String, String> options,
                           Callback<TwilioIceResponse> tokenCallback);
    }

    private static class TwilioAuthorizationInterceptor implements RequestInterceptor {
        private static final String AUTH_USERNAME = "twilio";
        private static final String AUTH_PASSWORD = "video";

        @Override
        public void intercept(RequestFacade requestFacade) {
            requestFacade.addHeader("Authorization", getAuthValue());
        }

        private String getAuthValue() {
            final String authString = AUTH_USERNAME + ":" + AUTH_PASSWORD;
            return "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        }
    }

    private static SimpleSignalingApi simpleSignalingService = new RestAdapter.Builder()
            .setEndpoint("https://simple-signaling.appspot.com")
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .setConverter(new GsonConverter(new GsonBuilder().create()))
            .build()
            .create(SimpleSignalingApi.class);


    public static void getAccessToken(String username, String realm,
                                      Callback<String> callback) {
        HashMap<String,String> options = new HashMap<>();
        options.put(REALM, realm);
        options.put("identity", username);
        options.put("ttl", TTL);
        simpleSignalingService.getAccessToken(options, callback);
    }


    public static void getIceServers(String realm, Callback<TwilioIceResponse> callback) {
        HashMap<String,String> options = new HashMap<>();
        options.put(REALM, realm);
        simpleSignalingService.getIceServers(options, callback);
    }
}
