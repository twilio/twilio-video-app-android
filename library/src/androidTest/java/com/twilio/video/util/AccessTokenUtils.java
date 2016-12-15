package com.twilio.video.util;

import android.util.Base64;

import com.twilio.video.test.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class AccessTokenUtils {
    public static final String P2P = "P2P";
    public static final String SFU = "SFU";
    private static final String PROD = "prod";
    private static final String ENVIRONMENT = "environment";
    private static final String IDENTITY = "identity";
    private static final String TTL = "ttl";
    private static final String TTL_DEFAULT = "3000";
    private static final String CONFIGURATION_PROFILE_SID = "configurationProfileSid";
    public static final String STAGE = "stage";
    public static final String DEV= "dev";

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

    public static String getAccessToken(String username) {
        HashMap<String,String> options = new HashMap<>();
        options.put(ENVIRONMENT, BuildConfig.ENVIRONMENT);
        options.put(IDENTITY, username);
        options.put(TTL, TTL_DEFAULT);
        options.put(CONFIGURATION_PROFILE_SID, getProfileConfigSid(BuildConfig.ENVIRONMENT,
                    BuildConfig.TOPOLOGY));
        return tokenService.obtainTwilioCapabilityToken(options);
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
