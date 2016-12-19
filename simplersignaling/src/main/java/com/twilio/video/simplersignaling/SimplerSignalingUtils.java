package com.twilio.video.simplersignaling;

import android.util.Base64;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public class SimplerSignalingUtils {
    private static final String ENVIRONMENT = "environment";
    private static final String IDENTITY = "identity";
    private static final String TTL = "ttl";
    /*
     * The default is usually 30 minutes. We are intentionally setting it to 5 minutes to validate
     * expiration.
     */
    private static final String TTL_DEFAULT = "300";
    private static final String CONFIGURATION_PROFILE_SID = "configurationProfileSid";
    private static final String PROD = "prod";
    public static final String STAGE = "stage";
    public static final String DEV = "dev";
    public static final String P2P = "P2P";
    public static final String SFU = "SFU";

    class Configuration {
        public JsonObject configurationProfileSids;
    }

    /* Define the Retrofit Token Service */
    interface SimplerSignalingApi {
        @GET("/access-token")
        void getAccessToken(@QueryMap Map<String, String> options,
                            Callback<String> tokenCallback);

        @GET("/configuration")
        void getConfiguration(@QueryMap Map<String, String> options,
                              Callback<Configuration> configurationCallback);
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

    public static void getAccessToken(final String username,
                                      final String environment,
                                      final String topology,
                                      final Callback<String> callback) {

        final HashMap<String,String> options = new HashMap<>();
        options.put(ENVIRONMENT, environment);

        simplerSignalingService.getConfiguration(options, new Callback<Configuration>() {
            @Override
            public void success(Configuration configuration, Response response) {
                String configurationProfileSid = getConfigurationProfileSid(configuration, topology);

                options.put(IDENTITY, username);
                options.put(TTL, TTL_DEFAULT);
                options.put(CONFIGURATION_PROFILE_SID, configurationProfileSid);

                simplerSignalingService.getAccessToken(options, callback);
            }

            @Override
            public void failure(RetrofitError error) {
                // Return the error from attempting to obtain the configuration
                callback.failure(error);
            }
        });

    }

    // Provide a synchronous version of getAccessToken for tests
    public static String getAccessToken(final String username,
                                        final String environment,
                                        final String topology) {

        final CountDownLatch callbackLatch = new CountDownLatch(1);
        final String[] token = new String[1];

        getAccessToken(username, environment, topology, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                token[0] = s;
                callbackLatch.countDown();
            }

            @Override
            public void failure(RetrofitError error) {
                token[0] = null;
                callbackLatch.countDown();
            }
        });

        try {
            callbackLatch.await();
        } catch (InterruptedException e) {
            return null;
        }

        return token[0];
    }

    private static String getConfigurationProfileSid(Configuration configuration, String topology) {
        for(Map.Entry<String, JsonElement> entry : configuration.configurationProfileSids.entrySet()) {
            if(topology.equals(entry.getValue().getAsString())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
