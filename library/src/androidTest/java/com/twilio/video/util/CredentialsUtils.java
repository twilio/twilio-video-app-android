/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.util;


import com.twilio.video.test.BuildConfig;
import com.twilio.video.token.VideoAccessToken;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class CredentialsUtils {
    private static final int TTL_DEFAULT = 1800;
    public static final String ACCOUNT_SID = "account_sid";
    public static final String API_KEY = "api_key";
    public static final String API_KEY_SECRET = "api_key_secret";
    public static final String DEV_ACCOUNT_SID = "dev_account_sid";
    public static final String DEV_API_KEY = "dev_api_key";
    public static final String DEV_API_KEY_SECRET = "dev_api_key_secret";
    public static final String STAGE_ACCOUNT_SID = "stage_account_sid";
    public static final String STAGE_API_KEY = "stage_api_key";
    public static final String STAGE_API_KEY_SECRET = "stage_api_key_secret";
    public static final String TWILIO_VIDEO_JSON_NOT_PROVIDED = "library/twilio-video.json is " +
            "required to create tokens for library tests that connect to a Room";

    public static String getAccessToken(String username, Topology topology) {
        Preconditions.checkNotNull(BuildConfig.twilioCredentials,
                TWILIO_VIDEO_JSON_NOT_PROVIDED);
        Map<String, String> credentials = resolveCredentials(
                Environment.fromString(BuildConfig.ENVIRONMENT));
        VideoAccessToken videoAccessToken = new VideoAccessToken.Builder(
                credentials.get(ACCOUNT_SID),
                credentials.get(API_KEY),
                credentials.get(API_KEY_SECRET))
                .identity(username)
                .ttl(TTL_DEFAULT)
                .build();

        return videoAccessToken.getJwt();
    }

    public static Map<String, String> resolveCredentials(Environment environment) {
        Map<String, String> credentials = new HashMap<>();

        switch (environment) {
            case DEV:
                checkCredentialDefined(DEV_ACCOUNT_SID);
                checkCredentialDefined(DEV_API_KEY);
                checkCredentialDefined(DEV_API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(DEV_ACCOUNT_SID));
                credentials.put(API_KEY, BuildConfig.twilioCredentials.get(DEV_API_KEY));
                credentials.put(API_KEY_SECRET,
                        BuildConfig.twilioCredentials.get(DEV_API_KEY_SECRET));
                break;
            case STAGE:
                checkCredentialDefined(STAGE_ACCOUNT_SID);
                checkCredentialDefined(STAGE_API_KEY);
                checkCredentialDefined(STAGE_API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(STAGE_ACCOUNT_SID));
                credentials.put(API_KEY, BuildConfig.twilioCredentials.get(STAGE_API_KEY));
                credentials.put(API_KEY_SECRET,
                        BuildConfig.twilioCredentials.get(STAGE_API_KEY_SECRET));

                break;
            case PROD:
                checkCredentialDefined(ACCOUNT_SID);
                checkCredentialDefined(API_KEY);
                checkCredentialDefined(API_KEY_SECRET);

                credentials.put(ACCOUNT_SID, BuildConfig.twilioCredentials.get(ACCOUNT_SID));
                credentials.put(API_KEY, BuildConfig.twilioCredentials.get(API_KEY));
                credentials.put(API_KEY_SECRET, BuildConfig.twilioCredentials.get(API_KEY_SECRET));

                break;
            default:
                throw new RuntimeException("Unknown environment");
        }

        return credentials;
    }

    private static void checkCredentialDefined(String credentialKey) {
        assertTrue("Credential map does not contain key: " + credentialKey,
            BuildConfig.twilioCredentials.containsKey(credentialKey));
        assertFalse("Credential " + credentialKey + " must not be null or empty",
            StringUtils.isNullOrEmpty(BuildConfig.twilioCredentials.get(credentialKey)));
    }
}
