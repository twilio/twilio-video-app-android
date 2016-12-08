package com.twilio.video.base;

import android.support.test.InstrumentationRegistry;

import com.twilio.video.BuildConfig;
import com.twilio.video.env.Env;

import org.junit.Before;

import static junit.framework.Assert.assertEquals;

public abstract class BaseClientTest {
    public static final String TWILIO_ENVIRONMENT_KEY = "TWILIO_ENVIRONMENT";

    @Before
    public void setup() throws InterruptedException {
        String twilioEnv;
        // The environment key uses different values than simple signaling
        switch(BuildConfig.ENVIRONMENT) {
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
        assertEquals(twilioEnv, Env.get(InstrumentationRegistry.getContext(), TWILIO_ENVIRONMENT_KEY));
    }

}
