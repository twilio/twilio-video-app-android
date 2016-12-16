package com.twilio.video.util;

import android.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twilio.video.test.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class SimplerSignalingUtils {
    private static final String ENDPOINT = "https://simpler-signaling.appspot.com";
    private static final String ENVIRONMENT = "environment";
    private static final String IDENTITY = "identity";
    private static final String TTL = "ttl";
    private static final String TTL_DEFAULT = "3000";
    private static final String CONFIGURATION_PROFILE_SID = "configurationProfileSid";
    public static final String P2P = "P2P";
    public static final String SFU = "SFU";
    public static final String PROD = "prod";
    public static final String STAGE = "stage";
    public static final String DEV = "dev";

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

    class Configuration {
        public JsonObject configurationProfileSids;
    }

    interface SimplerSignalingService {
        @GET("/access-token")
        String getAccessToken(@QueryMap Map<String, String> options);
        @GET("/configuration")
        Configuration getConfiguration(@QueryMap Map<String, String> options);
    }

    private static SimplerSignalingService simplerSignalingService = new RestAdapter.Builder()
            .setEndpoint(ENDPOINT)
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .build()
            .create(SimplerSignalingService.class);

    public static String getConfigurationProfileSid(String environment, String topology) {
        if(environment == null || topology == null) {
            return null;
        }
        HashMap<String,String> options = new HashMap<>();
        options.put(ENVIRONMENT, BuildConfig.ENVIRONMENT);
        JsonObject configurationProfileSidsJsonObject =
                simplerSignalingService.getConfiguration(options).configurationProfileSids;

        for(Map.Entry<String, JsonElement> entry : configurationProfileSidsJsonObject.entrySet()) {
            if(topology.equals(entry.getValue().getAsString())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getAccessToken(String username) {
        HashMap<String,String> options = new HashMap<>();
        options.put(ENVIRONMENT, BuildConfig.ENVIRONMENT);
        options.put(IDENTITY, username);
        options.put(TTL, TTL_DEFAULT);
        options.put(CONFIGURATION_PROFILE_SID, getConfigurationProfileSid(BuildConfig.ENVIRONMENT,
                    BuildConfig.TOPOLOGY));
        return simplerSignalingService.getAccessToken(options);
    }

}
