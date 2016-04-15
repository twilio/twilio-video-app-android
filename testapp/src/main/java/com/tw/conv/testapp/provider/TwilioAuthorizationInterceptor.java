package com.tw.conv.testapp.provider;

import android.util.Base64;

import retrofit.RequestInterceptor;

public class TwilioAuthorizationInterceptor implements RequestInterceptor {
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
