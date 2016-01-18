package com.twilio.rtc.conversations.sdktests.provider;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class TCCapabilityTokenProvider {
    public static final String CAPABILITY_TOKEN = "capability_token";
    public static final String USERNAME = "username";
    public static final String TTL = "300"; //The default is usually 30 minutes. We are intentionally setting it to 5 minutes to validate expiration.

    /* Define the Retrofit Token Service */
    interface TokenService {
        @GET("/access-token")
        void obtainTwilioCapabilityToken(@QueryMap Map<String, String> options, Callback<String> tokenCallback);
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

    private static TokenService tokenService = new RestAdapter.Builder()
                .setEndpoint("https://simple-signaling.appspot.com")
                .setRequestInterceptor(new TwilioAuthorizationInterceptor())
                .build()
                .create(TokenService.class);

    public static void obtainTwilioCapabilityToken(String username, Callback<String> callback) {
        HashMap<String,String> options = new HashMap<>();
        options.put("realm", "prod");
        options.put("identity", username);
        options.put("ttl", TTL);
        tokenService.obtainTwilioCapabilityToken(options, callback);
    }
}
