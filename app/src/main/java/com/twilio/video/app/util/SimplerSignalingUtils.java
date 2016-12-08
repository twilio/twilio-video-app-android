package com.twilio.video.app.util;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.twilio.video.app.model.TwilioIceResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class SimplerSignalingUtils {
    private static final String P2P = "P2P";
    private static final String PROD = "prod";
    private static final String ENVIRONMENT = "environment";
    private static final String IDENTITY = "identity";
    private static final String TTL = "ttl";
    /*
     * The default is usually 30 minutes. We are intentionally setting it to 5 minutes to validate
     * expiration.
     */
    private static final String TTL_DEFAULT = "300";
    private static final String CONFIGURATION_PROFILE_SID = "configurationProfileSid";
    public static final String STAGE = "stage";
    public static final String DEV= "dev";



    /* Define the Retrofit Token Service */
    interface SimplerSignalingApi {
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

    private static SimplerSignalingApi simplerSignalingService = new RestAdapter.Builder()
            .setEndpoint("https://simpler-signaling.appspot.com")
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .setConverter(new GsonConverter(new GsonBuilder().create()))
            .build()
            .create(SimplerSignalingApi.class);

    public static void getAccessToken(String username,
                                      String environment,
                                      String topology,
                                      Callback<String> callback) {
        HashMap<String,String> options = new HashMap<>();
        options.put(ENVIRONMENT, environment);
        options.put(IDENTITY, username);
        options.put(TTL, TTL_DEFAULT);
        options.put(CONFIGURATION_PROFILE_SID, getProfileConfigSid(environment, topology));
        simplerSignalingService.getAccessToken(options, callback);
    }

    private static String getProfileConfigSid(String environment, String topology) {
        boolean isP2P = topology.equals(P2P);
        switch(environment) {
            case DEV:
                return isP2P ?
                        "VSbf4c8aee1e259d11b2c5adeebb7c0dbe" :
                        "VS6469e95f0b2e2c8f931086988d69f815";
            case STAGE:
                return isP2P ?
                        "VS0d1c1b07fafbe94b73670b37e7aedfbb" :
                        "VS395e1a612a6e3c63100a3b4d99d52265";
            case PROD:
            default:
                return isP2P ?
                        "VS3f75e0f14e7c8b20938fc5092e82f23a" :
                        "VS25275758820071c0d42246c538bc11ad";
        }
    }

}
