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

package com.twilio.video.twilioapi;

import android.util.Base64;
import com.google.gson.GsonBuilder;
import com.twilio.video.twilioapi.model.TwilioServiceToken;
import retrofit.Callback;
import retrofit.RestAdapter;
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
    void getServiceToken(
        @Header("Authorization") String authorization,
        @Path("accountSid") String accountSid,
        Callback<TwilioServiceToken> serviceTokenCallback);

    @POST("/2010-04-01/Accounts/{accountSid}/Tokens.json")
    TwilioServiceToken getServiceToken(
        @Header("Authorization") String authorization, @Path("accountSid") String accountSid);
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

  public static void getServiceToken(
      String accountSid,
      String signingKeySid,
      String signingKeySecret,
      String environment,
      Callback<TwilioServiceToken> callback)
      throws IllegalArgumentException {
    if (!environment.equalsIgnoreCase(PROD)
        && !environment.equalsIgnoreCase(STAGE)
        && !environment.equalsIgnoreCase(DEV)) {
      throw new IllegalArgumentException("Invalid Environment!");
    }
    if (!currentEnvironment.equalsIgnoreCase(environment)) {
      currentEnvironment = environment;
      twilioApiService = createTwilioApiService();
    }
    String authString = signingKeySid + ":" + signingKeySecret;
    String authorization = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
    twilioApiService.getServiceToken(authorization, accountSid, callback);
  }

  // Provide a synchronous version of getServiceToken for tests
  public static TwilioServiceToken getServiceToken(
      String accountSid, String signingKeySid, String signingKeySecret, String environment) {
    if (!environment.equalsIgnoreCase(PROD)
        && !environment.equalsIgnoreCase(STAGE)
        && !environment.equalsIgnoreCase(DEV)) {
      throw new IllegalArgumentException("Invalid Environment!");
    }
    if (!currentEnvironment.equalsIgnoreCase(environment)) {
      currentEnvironment = environment;
      twilioApiService = createTwilioApiService();
    }

    String authString = signingKeySid + ":" + signingKeySecret;
    String authorization = "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);

    return twilioApiService.getServiceToken(authorization, accountSid);
  }
}
