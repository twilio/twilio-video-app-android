package com.twilio.video.app.data;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.model.Topology;
import com.twilio.video.token.VideoAccessToken;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Single;

public class DevelopmentTokenService implements TokenService {
    private static final String ACCOUNT_SID = "account_sid";
    private static final String API_KEY = "api_key";
    private static final String API_KEY_SECRET = "api_key_secret";
    private static final String CONFIGURATION_PROFILE_SID = "configuration_profile_sid";
    private static final String SFU_CONFIGURATION_PROFILE_SID = "sfu_configuration_profile_sid";
    private static final String SFU_RECORDING_CONFIGURATION_PROFILE_SID =
            "sfu_recording_configuration_profile_sid";
    private static final String TWILIO_VIDEO_APP_JSON_NOT_PROVIDED = "app/twilio-video-app.json is " +
            "required to create tokens for development variant";

    @Override
    public Single<String> getToken(final String identity, Topology topology) {
        Preconditions.checkNotNull(BuildConfig.twilioCredentials,
                TWILIO_VIDEO_APP_JSON_NOT_PROVIDED);
        final Map<String, String> credentials = resolveCredentials(topology);

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return new VideoAccessToken.Builder(credentials.get(ACCOUNT_SID),
                        credentials.get(API_KEY),
                        credentials.get(API_KEY_SECRET))
                        .identity(identity)
                        .configurationProfileSid(credentials.get(CONFIGURATION_PROFILE_SID))
                        .build()
                        .getJwt();
            }
        });
    }

    private static Map<String, String> resolveCredentials(Topology topology) {
        Map<String, String> credentials = new HashMap<>();

        checkCredentialDefined(ACCOUNT_SID);
        checkCredentialDefined(API_KEY);
        checkCredentialDefined(API_KEY_SECRET);

        credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(ACCOUNT_SID));
        credentials.put(API_KEY, BuildConfig.twilioCredentials.get(API_KEY));
        credentials.put(API_KEY_SECRET, BuildConfig.twilioCredentials.get(API_KEY_SECRET));

        switch (topology) {
            case P2P:
                checkCredentialDefined(CONFIGURATION_PROFILE_SID);

                credentials.put(CONFIGURATION_PROFILE_SID,
                        BuildConfig.twilioCredentials.get(CONFIGURATION_PROFILE_SID));
                break;
            case SFU:
                checkCredentialDefined(SFU_CONFIGURATION_PROFILE_SID);

                credentials.put(CONFIGURATION_PROFILE_SID,
                        BuildConfig.twilioCredentials.get(SFU_CONFIGURATION_PROFILE_SID));
                break;
            case SFU_RECORDING:
                checkCredentialDefined(SFU_RECORDING_CONFIGURATION_PROFILE_SID);

                credentials.put(CONFIGURATION_PROFILE_SID,
                        BuildConfig.twilioCredentials
                                .get(SFU_RECORDING_CONFIGURATION_PROFILE_SID));
                break;
            default:
                throw new RuntimeException("Unknown topology");
        }

        return credentials;
    }

    private static void checkCredentialDefined(String credentialKey) {
        Preconditions.checkState(BuildConfig.twilioCredentials.containsKey(credentialKey),
                "Credential map does not contain key: " + credentialKey);
        Preconditions.checkState(!Strings
                        .isNullOrEmpty(BuildConfig.twilioCredentials.get(credentialKey)),
                "Credential " + credentialKey + " must not be null or empty");
    }
}
