package com.twilio.video.twilioapi;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.twilio.video.twilioapi.model.TwilioServiceToken;

import java.util.concurrent.CountDownLatch;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public class TwilioApiUtils {
    private static final String PROD_BASE_URL = "https://api.twilio.com";
    private static final String STAGE_BASE_URL = "https://api.stage.twilio.com";
    private static final String DEV_BASE_URL = "https://api.dev.twilio.com";

    public static final String PROD = "prod";
    public static final String STAGE = "stage";
    public static final String DEV = "dev";

    private static String currentEnvironment = PROD;

    interface TwilioApiService {
        @POST("/2010-04-01/Accounts/{accountSid}/Tokens.json")
        void getServiceToken(@Header("Authorization") String authorization,
                             @Path("accountSid") String accountSid,
                             Callback<TwilioServiceToken> serviceTokenCallback);

        @POST("/2010-04-01/Accounts/{accountSid}/Tokens.json")
        TwilioServiceToken getServiceToken(@Header("Authorization") String authorization,
                                           @Path("accountSid") String accountSid);
    }

    private static TwilioApiService twilioApiService = createTwilioApiService();

    private static TwilioApiService createTwilioApiService() {
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
            .create(TwilioApiService.class);
    }

    public static void getServiceToken(String accountSid,
                                       String authToken,
                                       String environment,
                                       Callback<TwilioServiceToken> callback)
            throws IllegalArgumentException {
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            twilioApiService = createTwilioApiService();
        }
        twilioApiService.getServiceToken(accountSid, authToken);
    }

    // Provide a synchronous version of getServiceToken for tests
    public static TwilioServiceToken getServiceToken(String accountSid,
                                                     String authToken,
                                                     String environment) {
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            twilioApiService = createTwilioApiService();
        }

        String authString = accountSid + ":" + authToken;
        String authorization = "Basic " + Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        return twilioApiService.getServiceToken(authorization, accountSid);
    }
}
