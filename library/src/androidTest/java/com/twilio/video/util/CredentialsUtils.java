package com.twilio.video.util;

import android.support.test.espresso.core.deps.guava.base.Strings;

import com.twilio.video.test.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class CredentialsUtils {
    private static final int TTL_DEFAULT = 1800;
    public static final String ACCOUNT_SID = "account_sid";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String API_KEY = "api_key";
    public static final String API_KEY_SECRET = "api_key_secret";
    public static final String CONFIGURATION_PROFILE_SID = "configuration_profile_sid";
    public static final String SFU_CONFIGURATION_PROFILE_SID = "sfu_configuration_profile_sid";
    public static final String SFU_RECORDING_CONFIGURATION_PROFILE_SID =
            "sfu_recording_configuration_profile_sid";
    public static final String DEV_ACCOUNT_SID = "dev_account_sid";
    public static final String DEV_AUTH_TOKEN = "dev_auth_token";
    public static final String DEV_API_KEY = "dev_api_key";
    public static final String DEV_API_KEY_SECRET = "dev_api_key_secret";
    public static final String DEV_P2P_CONFIGURATION_PROFILE_SID =
            "dev_p2p_configuration_profile_sid";
    public static final String DEV_SFU_CONFIGURATION_PROFILE_SID =
            "dev_sfu_configuration_profile_sid";
    public static final String DEV_SFU_RECORDING_CONFIGURATION_PROFILE_SID =
            "dev_sfu_recording_configuration_profile_sid";
    public static final String STAGE_ACCOUNT_SID = "stage_account_sid";
    public static final String STAGE_AUTH_TOKEN = "stage_auth_token";
    public static final String STAGE_API_KEY = "stage_api_key";
    public static final String STAGE_API_KEY_SECRET = "stage_api_key_secret";
    public static final String STAGE_P2P_CONFIGURATION_PROFILE_SID =
            "stage_p2p_configuration_profile_sid";
    public static final String STAGE_SFU_CONFIGURATION_PROFILE_SID =
            "stage_sfu_configuration_profile_sid";
    public static final String STAGE_SFU_RECORDING_CONFIGURATION_PROFILE_SID =
            "stage_sfu_recording_configuration_profile_sid";

    public static String getAccessToken(String username) {
        Map<String, String> credentials = resolveCredentials(
                Environment.fromString(BuildConfig.ENVIRONMENT),
                Topology.fromString(BuildConfig.TOPOLOGY));
        VideoAccessToken videoAccessToken = new VideoAccessToken.Builder(
                credentials.get(ACCOUNT_SID),
                credentials.get(API_KEY),
                credentials.get(API_KEY_SECRET),
                credentials.get(CONFIGURATION_PROFILE_SID))
                .identity(username)
                .ttl(TTL_DEFAULT)
                .build();

        return videoAccessToken.getJwt();
    }

    public static Map<String, String> resolveCredentials(Environment environment,
                                                         Topology topology) {
        Map<String, String> credentials = new HashMap<>();

        switch (environment) {
            case DEV:
                checkCredentialDefined(DEV_ACCOUNT_SID);
                checkCredentialDefined(DEV_AUTH_TOKEN);
                checkCredentialDefined(DEV_API_KEY);
                checkCredentialDefined(DEV_API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(DEV_ACCOUNT_SID));
                credentials.put(AUTH_TOKEN, BuildConfig.twilioCredentials.get(DEV_AUTH_TOKEN));
                credentials.put(API_KEY, BuildConfig.twilioCredentials.get(DEV_API_KEY));
                credentials.put(API_KEY_SECRET,
                        BuildConfig.twilioCredentials.get(DEV_API_KEY_SECRET));

                switch (topology) {
                    case P2P:
                        checkCredentialDefined(DEV_P2P_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(DEV_P2P_CONFIGURATION_PROFILE_SID));
                        break;
                    case SFU:
                        checkCredentialDefined(DEV_SFU_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(DEV_SFU_CONFIGURATION_PROFILE_SID));
                        break;
                    case SFU_RECORDING:
                        checkCredentialDefined(DEV_SFU_RECORDING_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(DEV_SFU_RECORDING_CONFIGURATION_PROFILE_SID));
                        break;
                    default:
                        throw new RuntimeException("Unknown topology");
                }

                break;
            case STAGE:
                checkCredentialDefined(STAGE_ACCOUNT_SID);
                checkCredentialDefined(STAGE_AUTH_TOKEN);
                checkCredentialDefined(STAGE_API_KEY);
                checkCredentialDefined(STAGE_API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(STAGE_ACCOUNT_SID));
                credentials.put(AUTH_TOKEN, BuildConfig.twilioCredentials.get(STAGE_AUTH_TOKEN));
                credentials.put(API_KEY, BuildConfig.twilioCredentials.get(STAGE_API_KEY));
                credentials.put(API_KEY_SECRET,
                        BuildConfig.twilioCredentials.get(STAGE_API_KEY_SECRET));

                switch (topology) {
                    case P2P:
                        checkCredentialDefined(STAGE_P2P_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(STAGE_P2P_CONFIGURATION_PROFILE_SID));
                        break;
                    case SFU:
                        checkCredentialDefined(STAGE_SFU_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(STAGE_SFU_CONFIGURATION_PROFILE_SID));
                        break;
                    case SFU_RECORDING:
                        checkCredentialDefined(STAGE_SFU_RECORDING_CONFIGURATION_PROFILE_SID);

                        credentials.put(CONFIGURATION_PROFILE_SID,
                                BuildConfig.twilioCredentials
                                        .get(STAGE_SFU_RECORDING_CONFIGURATION_PROFILE_SID));
                        break;
                    default:
                        throw new RuntimeException("Unknown topology");
                }

                break;
            case PROD:
                checkCredentialDefined(ACCOUNT_SID);
                checkCredentialDefined(AUTH_TOKEN);
                checkCredentialDefined(API_KEY);
                checkCredentialDefined(API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(ACCOUNT_SID));
                credentials.put(AUTH_TOKEN, BuildConfig.twilioCredentials.get(AUTH_TOKEN));
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

                break;
            default:
                throw new RuntimeException("Unknown environment");
        }

        return credentials;
    }

    private static void checkCredentialDefined(String credentialKey) {
        /*
         * Not all developers will have SFU and SFU_RECORDING configuration profile sids
         * so we should ignore tests that rely on these sids and fail if any
         * other credentials are not set.
         */
        if (credentialKey.equals(SFU_CONFIGURATION_PROFILE_SID) ||
                credentialKey.equals(SFU_RECORDING_CONFIGURATION_PROFILE_SID)) {
            assumeTrue("Credential map does not contain key: " + credentialKey,
                    BuildConfig.twilioCredentials.containsKey(credentialKey));
            assumeFalse("Credential " + credentialKey + " must not be null or empty",
                    Strings.isNullOrEmpty(BuildConfig.twilioCredentials.get(credentialKey)));

        } else {
            assertTrue("Credential map does not contain key: " + credentialKey,
                    BuildConfig.twilioCredentials.containsKey(credentialKey));
            assertFalse("Credential " + credentialKey + " must not be null or empty",
                    Strings.isNullOrEmpty(BuildConfig.twilioCredentials.get(credentialKey)));
        }
    }
}
