package com.twilio.video.util;

import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class AccessTokenUtils {
    private static final String TTL = "3000";

    // Define the Retrofit Token Service
    interface TokenService {
        @GET("/access-token")
        String obtainTwilioCapabilityToken(@QueryMap Map<String, String> options);
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
            .setEndpoint("https://simpler-signaling.appspot.com")
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .build()
            .create(TokenService.class);

    public static String getAccessToken(String username, String realm) {
        HashMap<String,String> options = new HashMap<>();
        options.put("environment", realm);
        options.put("identity", username);
        options.put("ttl", TTL);
        return tokenService.obtainTwilioCapabilityToken(options);
    }
}
