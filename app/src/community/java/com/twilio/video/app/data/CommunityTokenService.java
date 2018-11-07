package com.twilio.video.app.data;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.model.RoomProperties;
import com.twilio.video.token.VideoAccessToken;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CommunityTokenService implements TokenService {
    private static final String ACCOUNT_SID = "account_sid";
    private static final String API_KEY = "api_key";
    private static final String API_KEY_SECRET = "api_key_secret";
    private static final String TWILIO_VIDEO_APP_JSON_NOT_PROVIDED =
            "app/twilio-video-app.json is " + "required to create tokens for development variant";

    /*
     * TODO: Topology is ignored so the Room will be the default type setup for the account. Use
     * REST API to create a Room with topology and create token with Room SID.
     */
    @Override
    public Single<String> getToken(final String identity, final RoomProperties roomProperties) {
        Preconditions.checkNotNull(
                BuildConfig.twilioCredentials, TWILIO_VIDEO_APP_JSON_NOT_PROVIDED);
        final Map<String, String> credentials = resolveCredentials();

        return Single.fromCallable(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return new VideoAccessToken.Builder(
                                        credentials.get(ACCOUNT_SID),
                                        credentials.get(API_KEY),
                                        credentials.get(API_KEY_SECRET))
                                .identity(identity)
                                .build()
                                .getJwt();
                    }
                });
    }

    private static Map<String, String> resolveCredentials() {
        Map<String, String> credentials = new HashMap<>();

        checkCredentialDefined(ACCOUNT_SID);
        checkCredentialDefined(API_KEY);
        checkCredentialDefined(API_KEY_SECRET);

        credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(ACCOUNT_SID));
        credentials.put(API_KEY, BuildConfig.twilioCredentials.get(API_KEY));
        credentials.put(API_KEY_SECRET, BuildConfig.twilioCredentials.get(API_KEY_SECRET));

        return credentials;
    }

    private static void checkCredentialDefined(String credentialKey) {
        Preconditions.checkState(
                BuildConfig.twilioCredentials.containsKey(credentialKey),
                "Credential map does not contain key: " + credentialKey);
        Preconditions.checkState(
                !Strings.isNullOrEmpty(BuildConfig.twilioCredentials.get(credentialKey)),
                "Credential " + credentialKey + " must not be null or empty");
    }
}
