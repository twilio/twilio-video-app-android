/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.util;

import static com.twilio.video.app.data.api.TwilioApiEnvironmentKt.TWILIO_API_DEV_ENV;
import static com.twilio.video.app.data.api.TwilioApiEnvironmentKt.TWILIO_API_STAGE_ENV;

public class EnvUtil {
    private static final String TWILIO_DEV_ENV = "Development";
    private static final String TWILIO_STAGE_ENV = "Staging";
    private static final String TWILIO_PROD_ENV = "Production";
    public static final String TWILIO_ENV_KEY = "TWILIO_ENVIRONMENT";

    public static String getNativeEnvironmentVariableValue(String environment) {
        if (environment != null) {
            if (environment.equals(TWILIO_API_DEV_ENV)) {
                return TWILIO_DEV_ENV;
            } else if (environment.equals(TWILIO_API_STAGE_ENV)) {
                return TWILIO_STAGE_ENV;
            }
        }

        return TWILIO_PROD_ENV;
    }
}
