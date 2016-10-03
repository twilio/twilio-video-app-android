package com.twilio.video.app.util;

import android.util.Base64;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class SimplerSignalingUtils {
    public static final String REALM_KEY = "realm";
    public static final String IDENTITY_KEY = "identity";
    public static final String TTL_KEY = "ttl";

    /*
     * The default is usually 30 minutes. We are intentionally setting it to 5 minutes to validate
     * expiration.
     */
    public static final String TTL = "300";

    /* Define the Retrofit Token Service */
    interface SimpleSignalingApi {
        @GET("/access-token")
        String getAccessToken(@QueryMap Map<String, String> options);
        @GET("/access-token")
        void getAccessToken(@QueryMap Map<String, String> options,
                            Callback<String> tokenCallback);
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

    public static String getAccessToken(String username, String realm) {
        HashMap<String,String> options = new HashMap<>();
        options.put(REALM_KEY, realm);
        options.put(IDENTITY_KEY, username);
        options.put(TTL_KEY, TTL);

        return simpleSignalingService.getAccessToken(options);
    }
}
