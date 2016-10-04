package com.twilio.video.app.util;

import android.content.Context;

import com.twilio.common.AccessManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class AccessManagerHelper {


    public static AccessManager createAccessManager(Context context, String capabilityToken) {
        return new AccessManager(context, capabilityToken, createAccessManagerListener());
    }

    private static AccessManager.Listener createAccessManagerListener() {
        return new AccessManager.Listener() {
            @Override
            public void onTokenExpired(AccessManager accessManager) {
                Timber.d("onTokenExpired");
                obtainCapabilityToken(accessManager);
            }

            @Override
            public void onTokenUpdated(AccessManager accessManager) {
                Timber.d("onTokenUpdatedd");
            }

            @Override
            public void onError(AccessManager accessManager, String s) {
                Timber.e("onError: "+s);
            }
        };
    }

    private static void obtainCapabilityToken(final AccessManager accessManager) {
        if (accessManager == null) {
            Timber.e("AccessManager is null");
            return;
        }
        SimpleSignalingUtils.getAccessToken(
                accessManager.getIdentity(), "prod", new Callback<String>() {

                    @Override
                    public void success(final String capabilityToken, Response response) {
                        if (accessManager != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    accessManager.updateToken(capabilityToken);
                                }
                            }).start();
                        } else {
                            Timber.e("AccessManager is null");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.e("Error fetching new capability token: " +
                                error.getLocalizedMessage());

                    }
                });

    }
}
