package com.tw.conv.testapp.provider;

import android.util.Base64;

import com.google.gson.GsonBuilder;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;

public class TCIceServersProvider {
    public static final String REALM = "realm";

    /* Define the Retrofit Token Service */
    interface IceService {
        @GET("/ice")
        void obtainTwilioIceServers(Callback<TwilioIceServers> tokenCallback);
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

    private static IceService tokenService = new RestAdapter.Builder()
            .setEndpoint("http://pacificgrid.ngrok.io")
            .setRequestInterceptor(new TwilioAuthorizationInterceptor())
            .setConverter(new GsonConverter(new GsonBuilder().create()))
            .build()
            .create(IceService.class);


    public static void obtainTwilioIceServers(Callback<TwilioIceServers> callback) {
        tokenService.obtainTwilioIceServers(callback);
    }



}
