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

package com.twilio.video.base;

import static junit.framework.Assert.assertEquals;

import android.support.test.InstrumentationRegistry;
import com.twilio.video.Video;
import com.twilio.video.env.Env;
import com.twilio.video.test.BuildConfig;
import org.junit.Before;

public abstract class BaseVideoTest {
    public static final String TWILIO_ENVIRONMENT_KEY = "TWILIO_ENVIRONMENT";

    @Before
    public void setup() throws InterruptedException {
        String twilioEnv;
        // The environment key uses different values than simple signaling
        switch (BuildConfig.ENVIRONMENT) {
            case "prod":
                twilioEnv = "Production";
                break;
            case "stage":
                twilioEnv = "Staging";
                break;
            case "dev":
                twilioEnv = "Development";
                break;
            default:
                twilioEnv = "Production";
        }

        Env.set(InstrumentationRegistry.getContext(), TWILIO_ENVIRONMENT_KEY, twilioEnv, true);
        assertEquals(
                twilioEnv, Env.get(InstrumentationRegistry.getContext(), TWILIO_ENVIRONMENT_KEY));

        // Set log level
        Video.setLogLevel(BuildConfig.TEST_LOG_LEVEL);
    }
}
