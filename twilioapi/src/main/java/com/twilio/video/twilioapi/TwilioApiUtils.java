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
import retrofit.http.POST;
import retrofit.http.Path;

public class TwilioApiUtils {
    private static final String PROD_BASE_URL = "https://api.twilio.com";
    private static final String STAGE_BASE_URL = "https://api.stage.twilio.com";
    private static final String DEV_BASE_URL = "https://api.dev.twilio.com";

    private static final String PROD = "prod";
    private static final String STAGE = "stage";
    private static final String DEV = "dev";

    private static String currentEnvironment = PROD;


    interface TwilioApiService {
        @POST("/2010-04-01/Accounts/{accountSid}/Tokens.json")
        void getServiceToken(@Path("accountSid") String accountSid,
                             Callback<TwilioServiceToken> serviceTokenCallback);

        @POST("/2010-04-01/Accounts/{accountSid}/Tokens.json")
        TwilioServiceToken getServiceToken(@Path("accountSid") String accountSid);
    }

    private static class TwilioAuthorizationInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade requestFacade) {
            requestFacade.addHeader("Authorization", getAuthValue());
        }

        private String getAuthValue() {
            final String authString = BuildConfig.ACCOUNT_SID + ":" + BuildConfig.AUTH_TOKEN;
            return "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        }
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
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .setConverter(new GsonConverter(new GsonBuilder().create()))
            .build()
            .create(TwilioApiService.class);
    }

    public static void getServiceToken(String environment,
                                       Callback<TwilioServiceToken> callback) throws IllegalArgumentException{
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            twilioApiService = createTwilioApiService();
        }
        twilioApiService.getServiceToken(BuildConfig.ACCOUNT_SID, callback);
    }

    // Provide a synchronous version of getServiceToken for tests
    public static TwilioServiceToken getServiceToken(String environment) {
        if (!environment.equalsIgnoreCase(PROD) &&
            !environment.equalsIgnoreCase(STAGE) &&
            !environment.equalsIgnoreCase(DEV)){
            throw new IllegalArgumentException("Invalid Environment!");
        }
        if (!currentEnvironment.equalsIgnoreCase(environment)) {
            currentEnvironment = environment;
            twilioApiService = createTwilioApiService();
        }
        return twilioApiService.getServiceToken(BuildConfig.ACCOUNT_SID);
    }
}
